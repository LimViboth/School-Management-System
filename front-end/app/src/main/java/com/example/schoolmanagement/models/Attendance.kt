package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

enum class AttendanceStatus {
    @SerializedName("present") PRESENT,
    @SerializedName("absent") ABSENT,
    @SerializedName("late") LATE,
    @SerializedName("excused") EXCUSED
}

data class Attendance(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("marked_by") val markedBy: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("student_number") val studentNumber: String? = null
)

data class AttendanceCreate(
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String = "present",
    @SerializedName("notes") val notes: String? = null
)

data class AttendanceBulkCreate(
    @SerializedName("class_id") val classId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("attendance_records") val attendanceRecords: List<AttendanceRecord>
)

data class AttendanceRecord(
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("status") val status: String = "present",
    @SerializedName("notes") val notes: String? = null
)
