package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("profile_picture") val profilePicture: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class UserLogin(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UserRegister(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("role") val role: String = "teacher"
)

data class UserUpdate(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null
)

data class PasswordChange(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class Token(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)
