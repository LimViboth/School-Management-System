package com.example.schoolmanagement.repository

import com.example.schoolmanagement.api.ApiService
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class StudentRepository {

    private val apiService: ApiService = RetrofitClient.getApiService()
    private val gson = Gson()

    suspend fun getStudents(classId: Int? = null, search: String? = null): Resource<List<Student>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStudents(classId, search)
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

    suspend fun getStudent(studentId: Int): Resource<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStudent(studentId)
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

    suspend fun createStudent(studentCreate: StudentCreate): Resource<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createStudent(studentCreate)
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

    suspend fun updateStudent(studentId: Int, studentUpdate: StudentUpdate): Resource<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateStudent(studentId, studentUpdate)
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

    suspend fun deleteStudent(studentId: Int): Resource<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteStudent(studentId)
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
