package com.example.schoolmanagement.repository

import com.example.schoolmanagement.api.ApiService
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.api.TokenManager
import com.example.schoolmanagement.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository {

    private val apiService: ApiService = RetrofitClient.getApiService()
    private val tokenManager: TokenManager = RetrofitClient.getTokenManager()
    private val gson = Gson()

    suspend fun login(email: String, password: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(UserLogin(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!
                    tokenManager.saveToken(token.accessToken, token.tokenType)
                    
                    // Fetch user info after successful login
                    val userResponse = apiService.getCurrentUser()
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()!!
                        tokenManager.saveUserInfo(user.id, user.email, user.fullName, user.role)
                        Resource.Success(user)
                    } else {
                        Resource.Error(parseError(userResponse))
                    }
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun register(email: String, password: String, fullName: String, role: String = "teacher"): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(UserRegister(email, password, fullName, role))
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun getCurrentUser(): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(PasswordChange(currentPassword, newPassword))
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun changePassword(passwordChange: PasswordChange): Resource<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(passwordChange)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun updateUser(userId: Int, userUpdate: UserUpdate): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateUser(userId, userUpdate)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error(parseError(response))
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun logout(): Resource<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logout()
                tokenManager.clearAll()
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Success(MessageResponse("Logged out locally"))
                }
            } catch (e: Exception) {
                tokenManager.clearAll()
                Resource.Success(MessageResponse("Logged out locally"))
            }
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun getUserInfo(): Triple<String?, String?, String?> {
        return Triple(tokenManager.getUserName(), tokenManager.getUserEmail(), tokenManager.getUserRole())
    }

    private fun <T> parseError(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.detail
            } else {
                "Unknown error occurred"
            }
        } catch (e: Exception) {
            response.message() ?: "Unknown error occurred"
        }
    }
}
