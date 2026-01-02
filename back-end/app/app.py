from fastapi import FastAPI, Depends, HTTPException, status, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_
from datetime import timedelta, date
from typing import List, Optional

from app.db import get_db, init_db
from app.models import User, Class, Student, Attendance, Notification
from app.schemas import (
    UserCreate, UserResponse, UserUpdate, UserLogin, Token, PasswordChange,
    ClassCreate, ClassResponse, ClassUpdate, ClassWithStudentCount,
    StudentCreate, StudentResponse, StudentUpdate, StudentWithClass,
    AttendanceCreate, AttendanceResponse, AttendanceBulkCreate, AttendanceWithStudent,
    NotificationResponse, DashboardStats
)
from app.auth import (
    get_password_hash, verify_password, create_access_token,
    get_current_user, get_current_active_user, get_admin_user,
    ACCESS_TOKEN_EXPIRE_MINUTES
)

app = FastAPI(
    title="School Management System API",
    description="API for managing students, classes, attendance, and more",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Change to specific origins in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup():
    await init_db()


# ==================== AUTH ROUTES ====================

@app.post("/api/auth/register", response_model=UserResponse, tags=["Authentication"])
async def register(user: UserCreate, db: AsyncSession = Depends(get_db)):
    """Register a new user"""
    # Check if email exists
    result = await db.execute(select(User).where(User.email == user.email))
    if result.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="Email already registered")
    
    db_user = User(
        email=user.email,
        hashed_password=get_password_hash(user.password),
        full_name=user.full_name,
        role=user.role.value
    )
    db.add(db_user)
    await db.flush()
    await db.refresh(db_user)
    return db_user


@app.post("/api/auth/login", response_model=Token, tags=["Authentication"])
async def login(
    login_data: UserLogin,
    db: AsyncSession = Depends(get_db)
):
    """Login and get access token"""
    result = await db.execute(select(User).where(User.email == login_data.email))
    user = result.scalar_one_or_none()
    
    if not user or not verify_password(login_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
        )
    
    access_token = create_access_token(
        data={"sub": user.email, "user_id": user.id},
        expires_delta=timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    return {"access_token": access_token, "token_type": "bearer"}


@app.get("/api/auth/me", response_model=UserResponse, tags=["Authentication"])
async def get_me(current_user: User = Depends(get_current_active_user)):
    """Get current user info"""
    return current_user


@app.put("/api/auth/password", tags=["Authentication"])
async def change_password(
    password_data: PasswordChange,
    current_user: User = Depends(get_current_active_user),
    db: AsyncSession = Depends(get_db)
):
    """Change password"""
    if not verify_password(password_data.current_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="Incorrect current password")
    
    current_user.hashed_password = get_password_hash(password_data.new_password)
    await db.flush()
    return {"message": "Password updated successfully"}


@app.post("/api/auth/logout", tags=["Authentication"])
async def logout(current_user: User = Depends(get_current_active_user)):
    """Logout current user"""
    return {"message": "Successfully logged out"}


# ==================== USER ROUTES ====================

@app.get("/api/users", response_model=List[UserResponse], tags=["Users"])
async def get_users(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_admin_user)
):
    """Get all users (Admin only)"""
    result = await db.execute(select(User))
    return result.scalars().all()


@app.get("/api/users/{user_id}", response_model=UserResponse, tags=["Users"])
async def get_user(
    user_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_admin_user)
):
    """Get user by ID (Admin only)"""
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@app.put("/api/users/{user_id}", response_model=UserResponse, tags=["Users"])
async def update_user(
    user_id: int,
    user_update: UserUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Update user"""
    # Users can update themselves, admins can update anyone
    if current_user.id != user_id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Not authorized")
    
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    for field, value in user_update.model_dump(exclude_unset=True).items():
        setattr(user, field, value)
    
    await db.flush()
    await db.refresh(user)
    return user


# ==================== CLASS ROUTES ====================

@app.post("/api/classes", response_model=ClassResponse, tags=["Classes"])
async def create_class(
    class_data: ClassCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Create a new class"""
    db_class = Class(**class_data.model_dump())
    if not db_class.teacher_id:
        db_class.teacher_id = current_user.id
    db.add(db_class)
    await db.flush()
    await db.refresh(db_class)
    return db_class


@app.get("/api/classes", response_model=List[ClassWithStudentCount], tags=["Classes"])
async def get_classes(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get all classes with student count"""
    # Get classes based on role
    if current_user.role == "admin":
        query = select(Class)
    else:
        query = select(Class).where(Class.teacher_id == current_user.id)
    
    result = await db.execute(query)
    classes = result.scalars().all()
    
    # Add student count to each class
    response = []
    for cls in classes:
        count_result = await db.execute(
            select(func.count(Student.id)).where(Student.class_id == cls.id)
        )
        student_count = count_result.scalar() or 0
        
        class_dict = {
            "id": cls.id,
            "name": cls.name,
            "description": cls.description,
            "grade_level": cls.grade_level,
            "academic_year": cls.academic_year,
            "teacher_id": cls.teacher_id,
            "created_at": cls.created_at,
            "updated_at": cls.updated_at,
            "student_count": student_count
        }
        response.append(ClassWithStudentCount(**class_dict))
    
    return response


@app.get("/api/classes/{class_id}", response_model=ClassResponse, tags=["Classes"])
async def get_class(
    class_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get class by ID"""
    result = await db.execute(select(Class).where(Class.id == class_id))
    cls = result.scalar_one_or_none()
    if not cls:
        raise HTTPException(status_code=404, detail="Class not found")
    return cls


@app.put("/api/classes/{class_id}", response_model=ClassResponse, tags=["Classes"])
async def update_class(
    class_id: int,
    class_update: ClassUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Update class"""
    result = await db.execute(select(Class).where(Class.id == class_id))
    cls = result.scalar_one_or_none()
    if not cls:
        raise HTTPException(status_code=404, detail="Class not found")
    
    for field, value in class_update.model_dump(exclude_unset=True).items():
        setattr(cls, field, value)
    
    await db.flush()
    await db.refresh(cls)
    return cls


@app.delete("/api/classes/{class_id}", tags=["Classes"])
async def delete_class(
    class_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_admin_user)
):
    """Delete class (Admin only)"""
    result = await db.execute(select(Class).where(Class.id == class_id))
    cls = result.scalar_one_or_none()
    if not cls:
        raise HTTPException(status_code=404, detail="Class not found")
    
    await db.delete(cls)
    return {"message": "Class deleted successfully"}


# ==================== STUDENT ROUTES ====================

@app.post("/api/students", response_model=StudentResponse, tags=["Students"])
async def create_student(
    student_data: StudentCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Create a new student"""
    # Check if student_id exists
    result = await db.execute(select(Student).where(Student.student_id == student_data.student_id))
    if result.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="Student ID already exists")
    
    db_student = Student(**student_data.model_dump())
    db.add(db_student)
    await db.flush()
    await db.refresh(db_student)
    return db_student


@app.get("/api/students", response_model=List[StudentResponse], tags=["Students"])
async def get_students(
    class_id: Optional[int] = Query(None),
    search: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get all students with optional filtering"""
    query = select(Student).where(Student.is_active == True)
    
    if class_id:
        query = query.where(Student.class_id == class_id)
    
    if search:
        search_term = f"%{search}%"
        query = query.where(
            (Student.first_name.ilike(search_term)) |
            (Student.last_name.ilike(search_term)) |
            (Student.student_id.ilike(search_term))
        )
    
    result = await db.execute(query)
    return result.scalars().all()


@app.get("/api/students/{student_id}", response_model=StudentWithClass, tags=["Students"])
async def get_student(
    student_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get student by ID"""
    result = await db.execute(select(Student).where(Student.id == student_id))
    student = result.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
    
    # Get class name
    class_name = None
    if student.class_id:
        class_result = await db.execute(select(Class).where(Class.id == student.class_id))
        cls = class_result.scalar_one_or_none()
        if cls:
            class_name = cls.name
    
    return StudentWithClass(
        **{k: v for k, v in student.__dict__.items() if not k.startswith('_')},
        class_name=class_name
    )


@app.put("/api/students/{student_id}", response_model=StudentResponse, tags=["Students"])
async def update_student(
    student_id: int,
    student_update: StudentUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Update student"""
    result = await db.execute(select(Student).where(Student.id == student_id))
    student = result.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
    
    for field, value in student_update.model_dump(exclude_unset=True).items():
        setattr(student, field, value)
    
    await db.flush()
    await db.refresh(student)
    return student


@app.delete("/api/students/{student_id}", tags=["Students"])
async def delete_student(
    student_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Delete student (soft delete)"""
    result = await db.execute(select(Student).where(Student.id == student_id))
    student = result.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
    
    student.is_active = False
    await db.flush()
    return {"message": "Student deleted successfully"}


# ==================== ATTENDANCE ROUTES ====================

@app.post("/api/attendance", response_model=AttendanceResponse, tags=["Attendance"])
async def create_attendance(
    attendance_data: AttendanceCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Mark attendance for a single student"""
    db_attendance = Attendance(
        **attendance_data.model_dump(),
        marked_by=current_user.id
    )
    db.add(db_attendance)
    await db.flush()
    await db.refresh(db_attendance)
    return db_attendance


@app.post("/api/attendance/bulk", tags=["Attendance"])
async def create_bulk_attendance(
    bulk_data: AttendanceBulkCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Mark attendance for multiple students at once"""
    created = []
    for record in bulk_data.attendance_records:
        # Check if attendance already exists for this student on this date
        existing = await db.execute(
            select(Attendance).where(
                and_(
                    Attendance.student_id == record["student_id"],
                    Attendance.class_id == bulk_data.class_id,
                    Attendance.date == bulk_data.date
                )
            )
        )
        existing_record = existing.scalar_one_or_none()
        
        if existing_record:
            # Update existing record
            existing_record.status = record.get("status", "present")
            existing_record.notes = record.get("notes")
            existing_record.marked_by = current_user.id
        else:
            # Create new record
            db_attendance = Attendance(
                student_id=record["student_id"],
                class_id=bulk_data.class_id,
                date=bulk_data.date,
                status=record.get("status", "present"),
                notes=record.get("notes"),
                marked_by=current_user.id
            )
            db.add(db_attendance)
        created.append(record["student_id"])
    
    await db.flush()
    return {"message": f"Attendance marked for {len(created)} students"}


@app.get("/api/attendance", response_model=List[AttendanceWithStudent], tags=["Attendance"])
async def get_attendance(
    class_id: int = Query(...),
    date_param: date = Query(..., alias="date"),
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get attendance for a class on a specific date"""
    result = await db.execute(
        select(Attendance).where(
            and_(
                Attendance.class_id == class_id,
                Attendance.date == date_param
            )
        )
    )
    attendance_records = result.scalars().all()
    
    # Enrich with student info
    response = []
    for record in attendance_records:
        student_result = await db.execute(select(Student).where(Student.id == record.student_id))
        student = student_result.scalar_one_or_none()
        if student:
            response.append(AttendanceWithStudent(
                id=record.id,
                student_id=record.student_id,
                class_id=record.class_id,
                date=record.date,
                status=record.status,
                notes=record.notes,
                marked_by=record.marked_by,
                created_at=record.created_at,
                student_name=f"{student.first_name} {student.last_name}",
                student_number=student.student_id
            ))
    
    return response


@app.get("/api/attendance/student/{student_id}", response_model=List[AttendanceResponse], tags=["Attendance"])
async def get_student_attendance(
    student_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get attendance history for a specific student"""
    result = await db.execute(
        select(Attendance).where(Attendance.student_id == student_id).order_by(Attendance.date.desc())
    )
    return result.scalars().all()


# ==================== DASHBOARD ROUTES ====================

@app.get("/api/dashboard/stats", response_model=DashboardStats, tags=["Dashboard"])
async def get_dashboard_stats(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get dashboard statistics"""
    # Total students
    student_count = await db.execute(select(func.count(Student.id)).where(Student.is_active == True))
    total_students = student_count.scalar() or 0
    
    # Total classes
    if current_user.role == "admin":
        class_count = await db.execute(select(func.count(Class.id)))
    else:
        class_count = await db.execute(
            select(func.count(Class.id)).where(Class.teacher_id == current_user.id)
        )
    total_classes = class_count.scalar() or 0
    
    # Total teachers
    teacher_count = await db.execute(
        select(func.count(User.id)).where(User.role == "teacher", User.is_active == True)
    )
    total_teachers = teacher_count.scalar() or 0
    
    # Today's attendance
    today = date.today()
    attendance_today = {"present": 0, "absent": 0, "late": 0, "excused": 0}
    
    for status in ["present", "absent", "late", "excused"]:
        count_result = await db.execute(
            select(func.count(Attendance.id)).where(
                and_(Attendance.date == today, Attendance.status == status)
            )
        )
        attendance_today[status] = count_result.scalar() or 0
    
    return DashboardStats(
        total_students=total_students,
        total_classes=total_classes,
        total_teachers=total_teachers,
        attendance_today=attendance_today,
        recent_attendance=[]
    )


# ==================== NOTIFICATION ROUTES ====================

@app.get("/api/notifications", response_model=List[NotificationResponse], tags=["Notifications"])
async def get_notifications(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Get user's notifications"""
    result = await db.execute(
        select(Notification)
        .where(Notification.user_id == current_user.id)
        .order_by(Notification.created_at.desc())
        .limit(50)
    )
    return result.scalars().all()


@app.put("/api/notifications/{notification_id}/read", tags=["Notifications"])
async def mark_notification_read(
    notification_id: int,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_active_user)
):
    """Mark notification as read"""
    result = await db.execute(
        select(Notification).where(
            and_(Notification.id == notification_id, Notification.user_id == current_user.id)
        )
    )
    notification = result.scalar_one_or_none()
    if not notification:
        raise HTTPException(status_code=404, detail="Notification not found")
    
    notification.is_read = True
    await db.flush()
    return {"message": "Notification marked as read"}

