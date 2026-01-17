package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

data class DashboardStats(
    @SerializedName("total_students") val totalStudents: Int,
    @SerializedName("total_classes") val totalClasses: Int,
    @SerializedName("total_teachers") val totalTeachers: Int,
    @SerializedName("attendance_today") val attendanceToday: AttendanceToday,
    @SerializedName("recent_attendance") val recentAttendance: List<Any>
)

data class AttendanceToday(
    @SerializedName("present") val present: Int = 0,
    @SerializedName("absent") val absent: Int = 0,
    @SerializedName("late") val late: Int = 0,
    @SerializedName("excused") val excused: Int = 0
)
