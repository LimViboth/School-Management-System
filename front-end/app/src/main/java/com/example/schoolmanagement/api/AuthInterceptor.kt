package com.example.schoolmanagement.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth header for login and register endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("login") || path.contains("register")) {
            return chain.proceed(originalRequest)
        }
        
        // Add authorization header if token exists
        val authHeader = tokenManager.getAuthorizationHeader()
        return if (authHeader != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
