package com.example.schoolmanagement.api

import com.example.schoolmanagement.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ENDPOINTS ====================

    @POST("api/auth/register")
    suspend fun register(@Body user: UserRegister): Response<User>

    @POST("api/auth/login")
    suspend fun login(@Body credentials: UserLogin): Response<Token>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>

    @PUT("api/auth/password")
    suspend fun changePassword(@Body passwordChange: PasswordChange): Response<MessageResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    // ==================== USER ENDPOINTS ====================

    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>

    @GET("api/users/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): Response<User>

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Int,
        @Body userUpdate: UserUpdate
    ): Response<User>

    // ==================== CLASS ENDPOINTS ====================

    @POST("api/classes")
    suspend fun createClass(@Body classData: ClassCreate): Response<ClassModel>

    @GET("api/classes")
    suspend fun getClasses(): Response<List<ClassModel>>

    @GET("api/classes/{classId}")
    suspend fun getClass(@Path("classId") classId: Int): Response<ClassModel>

    @PUT("api/classes/{classId}")
    suspend fun updateClass(
        @Path("classId") classId: Int,
        @Body classUpdate: ClassUpdate
    ): Response<ClassModel>

    @DELETE("api/classes/{classId}")
    suspend fun deleteClass(@Path("classId") classId: Int): Response<MessageResponse>

    // ==================== STUDENT ENDPOINTS ====================

    @POST("api/students")
    suspend fun createStudent(@Body student: StudentCreate): Response<Student>

    @GET("api/students")
    suspend fun getStudents(
        @Query("class_id") classId: Int? = null,
        @Query("search") search: String? = null
    ): Response<List<Student>>

    @GET("api/students/{studentId}")
    suspend fun getStudent(@Path("studentId") studentId: Int): Response<Student>

    @PUT("api/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: Int,
        @Body studentUpdate: StudentUpdate
    ): Response<Student>

    @DELETE("api/students/{studentId}")
    suspend fun deleteStudent(@Path("studentId") studentId: Int): Response<MessageResponse>

    // ==================== ATTENDANCE ENDPOINTS ====================

    @POST("api/attendance")
    suspend fun createAttendance(@Body attendance: AttendanceCreate): Response<Attendance>

    @POST("api/attendance/bulk")
    suspend fun createBulkAttendance(@Body bulkData: AttendanceBulkCreate): Response<MessageResponse>

    @GET("api/attendance")
    suspend fun getAttendance(
        @Query("class_id") classId: Int,
        @Query("date") date: String
    ): Response<List<Attendance>>

    @GET("api/attendance/student/{studentId}")
    suspend fun getStudentAttendance(@Path("studentId") studentId: Int): Response<List<Attendance>>

    // ==================== DASHBOARD ENDPOINTS ====================

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>

    // ==================== NOTIFICATION ENDPOINTS ====================

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @PUT("api/notifications/{notificationId}/read")
    suspend fun markNotificationRead(@Path("notificationId") notificationId: Int): Response<MessageResponse>
}
