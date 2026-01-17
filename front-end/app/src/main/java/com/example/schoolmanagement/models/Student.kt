package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    @SerializedName("class_id") val classId: Int?,
    @SerializedName("parent_name") val parentName: String?,
    @SerializedName("parent_phone") val parentPhone: String?,
    @SerializedName("parent_email") val parentEmail: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("class_name") val className: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}

data class StudentCreate(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("parent_name") val parentName: String? = null,
    @SerializedName("parent_phone") val parentPhone: String? = null,
    @SerializedName("parent_email") val parentEmail: String? = null
)

data class StudentUpdate(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("parent_name") val parentName: String? = null,
    @SerializedName("parent_phone") val parentPhone: String? = null,
    @SerializedName("parent_email") val parentEmail: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null
)
