package com.example.schoolmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private var progressBar: ProgressBar? = null
    
    private val authRepository by lazy { AuthRepository() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Check if already logged in
        if (authRepository.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            setLoading(true)
            
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome, ${result.data?.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@LoginActivity,
                        result.message ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }
            
            setLoading(false)
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

