package com.example.schoolmanagement

import android.app.Application
import com.example.schoolmanagement.api.RetrofitClient

class SchoolManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RetrofitClient with application context
        RetrofitClient.initialize(this)
    }
}
