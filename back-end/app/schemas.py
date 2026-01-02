from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
from datetime import datetime, date
from enum import Enum


# Enums
class UserRole(str, Enum):
    ADMIN = "admin"
    TEACHER = "teacher"


class AttendanceStatus(str, Enum):
    PRESENT = "present"
    ABSENT = "absent"
    LATE = "late"
    EXCUSED = "excused"


# Token Schemas
class Token(BaseModel):
    access_token: str
    token_type: str


class TokenData(BaseModel):
    email: Optional[str] = None
    user_id: Optional[int] = None


# User Schemas
class UserBase(BaseModel):
    email: EmailStr
    full_name: str
    role: UserRole = UserRole.TEACHER


class UserCreate(UserBase):
    password: str = Field(..., min_length=6)


class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    profile_picture: Optional[str] = None
    is_active: Optional[bool] = None


class UserResponse(BaseModel):
    id: int
    email: EmailStr
    full_name: str
    role: str
    profile_picture: Optional[str] = None
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class PasswordChange(BaseModel):
    current_password: str
    new_password: str = Field(..., min_length=6)


# Class Schemas
class ClassBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: Optional[str] = None
    grade_level: Optional[str] = None
    academic_year: Optional[str] = None


class ClassCreate(ClassBase):
    teacher_id: Optional[int] = None


class ClassUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    description: Optional[str] = None
    grade_level: Optional[str] = None
    academic_year: Optional[str] = None
    teacher_id: Optional[int] = None


class ClassResponse(ClassBase):
    id: int
    teacher_id: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class ClassWithStudentCount(ClassResponse):
    student_count: int = 0


# Student Schemas
class StudentBase(BaseModel):
    student_id: str = Field(..., min_length=1, max_length=50)
    first_name: str = Field(..., min_length=1, max_length=100)
    last_name: str = Field(..., min_length=1, max_length=100)
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    date_of_birth: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    parent_name: Optional[str] = None
    parent_phone: Optional[str] = None
    parent_email: Optional[EmailStr] = None


class StudentCreate(StudentBase):
    class_id: Optional[int] = None
    profile_picture: Optional[str] = None


class StudentUpdate(BaseModel):
    first_name: Optional[str] = Field(None, min_length=1, max_length=100)
    last_name: Optional[str] = Field(None, min_length=1, max_length=100)
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    date_of_birth: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    profile_picture: Optional[str] = None
    class_id: Optional[int] = None
    parent_name: Optional[str] = None
    parent_phone: Optional[str] = None
    parent_email: Optional[EmailStr] = None
    is_active: Optional[bool] = None


class StudentResponse(StudentBase):
    id: int
    profile_picture: Optional[str] = None
    class_id: Optional[int] = None
    is_active: bool
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class StudentWithClass(StudentResponse):
    class_name: Optional[str] = None


# Attendance Schemas
class AttendanceBase(BaseModel):
    student_id: int
    class_id: int
    date: date
    status: AttendanceStatus = AttendanceStatus.PRESENT
    notes: Optional[str] = None


class AttendanceCreate(AttendanceBase):
    pass


class AttendanceBulkCreate(BaseModel):
    class_id: int
    date: date
    attendance_records: List[dict]


class AttendanceUpdate(BaseModel):
    status: Optional[AttendanceStatus] = None
    notes: Optional[str] = None


class AttendanceResponse(AttendanceBase):
    id: int
    marked_by: Optional[int] = None
    created_at: datetime

    class Config:
        from_attributes = True


class AttendanceWithStudent(AttendanceResponse):
    student_name: str
    student_number: str


class AttendanceReport(BaseModel):
    class_id: int
    class_name: str
    date_from: date
    date_to: date
    total_students: int
    total_days: int
    attendance_summary: List[dict]


# Dashboard Schemas
class DashboardStats(BaseModel):
    total_students: int
    total_classes: int
    total_teachers: int
    attendance_today: dict
    recent_attendance: List[dict]


class NotificationBase(BaseModel):
    title: str
    message: str
    notification_type: Optional[str] = None


class NotificationCreate(NotificationBase):
    user_id: int


class NotificationResponse(NotificationBase):
    id: int
    user_id: int
    is_read: bool
    created_at: datetime

    class Config:
        from_attributes = True


class FileUploadResponse(BaseModel):
    filename: str
    url: str


class PaginatedResponse(BaseModel):
    items: List
    total: int
    page: int
    size: int
    pages: int
