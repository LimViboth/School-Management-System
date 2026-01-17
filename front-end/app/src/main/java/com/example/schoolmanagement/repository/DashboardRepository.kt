package com.example.schoolmanagement.repository

import com.example.schoolmanagement.api.ApiService
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class DashboardRepository {

    private val apiService: ApiService = RetrofitClient.getApiService()
    private val gson = Gson()

    suspend fun getDashboardStats(): Resource<DashboardStats> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboardStats()
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
