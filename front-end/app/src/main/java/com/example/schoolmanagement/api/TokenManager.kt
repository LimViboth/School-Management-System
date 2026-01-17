package com.example.schoolmanagement.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "school_management_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveToken(accessToken: String, tokenType: String = "bearer") {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getTokenType(): String {
        return prefs.getString(KEY_TOKEN_TYPE, "bearer") ?: "bearer"
    }

    fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null) "$type $token" else null
    }

    fun saveUserInfo(userId: Int, email: String, name: String, role: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
