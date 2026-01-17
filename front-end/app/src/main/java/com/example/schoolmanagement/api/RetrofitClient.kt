package com.example.schoolmanagement.api

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        if (tokenManager == null) {
            tokenManager = TokenManager(context.applicationContext)
        }
        
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager!!))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }

    fun getApiService(): ApiService {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first.")
        }
        
        if (apiService == null) {
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }

    fun getTokenManager(): TokenManager {
        if (tokenManager == null) {
            throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first.")
        }
        return tokenManager!!
    }

    fun clearInstance() {
        retrofit = null
        apiService = null
        tokenManager?.clearAll()
    }
}
