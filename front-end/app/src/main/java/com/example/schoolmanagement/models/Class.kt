package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

data class ClassModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("grade_level") val gradeLevel: String?,
    @SerializedName("academic_year") val academicYear: String?,
    @SerializedName("teacher_id") val teacherId: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("student_count") val studentCount: Int = 0
)

data class ClassCreate(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("academic_year") val academicYear: String? = null,
    @SerializedName("teacher_id") val teacherId: Int? = null
)

data class ClassUpdate(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("academic_year") val academicYear: String? = null,
    @SerializedName("teacher_id") val teacherId: Int? = null
)
