package com.example.schoolmanagement.models

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    @SerializedName("message") val message: String
)

data class ErrorResponse(
    @SerializedName("detail") val detail: String
)
