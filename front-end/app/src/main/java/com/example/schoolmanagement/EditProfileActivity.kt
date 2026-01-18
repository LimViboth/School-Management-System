package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.models.PasswordChange
import com.example.schoolmanagement.models.UserUpdate
import com.example.schoolmanagement.repository.AuthRepository
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private val authRepository by lazy { AuthRepository() }
    private val tokenManager by lazy { RetrofitClient.getTokenManager() }
    
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var tvRole: TextView
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutPasswordChange: LinearLayout
    private lateinit var btnTogglePasswordChange: Button
    
    private var userId: Int = 0
    private var isPasswordSectionVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        loadUserProfile()
        setupClickListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        tvRole = findViewById(R.id.tvRole)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
        layoutPasswordChange = findViewById(R.id.layoutPasswordChange)
        btnTogglePasswordChange = findViewById(R.id.btnTogglePasswordChange)

        // Email and role should not be editable
        etEmail.isEnabled = false
        
        // Initially hide password change section
        layoutPasswordChange.visibility = View.GONE
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            // Get cached user info
            val (name, email, role) = authRepository.getUserInfo()
            etFullName.setText(name)
            etEmail.setText(email)
            tvRole.text = role?.replaceFirstChar { it.uppercase() } ?: "Teacher"
            
            // Fetch fresh data from API
            when (val result = authRepository.getCurrentUser()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        userId = user.id
                        etFullName.setText(user.fullName)
                        etEmail.setText(user.email)
                        tvRole.text = user.role.replaceFirstChar { it.uppercase() }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@EditProfileActivity,
                        result.message ?: "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {}
            }
            
            progressBar.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        btnTogglePasswordChange.setOnClickListener {
            isPasswordSectionVisible = !isPasswordSectionVisible
            layoutPasswordChange.visibility = if (isPasswordSectionVisible) View.VISIBLE else View.GONE
            btnTogglePasswordChange.text = if (isPasswordSectionVisible) 
                "Hide Password Change" else "Change Password"
        }

        btnSaveProfile.setOnClickListener {
            if (validateProfileInput()) {
                updateProfile()
            }
        }

        btnChangePassword.setOnClickListener {
            if (validatePasswordInput()) {
                changePassword()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateProfileInput(): Boolean {
        if (etFullName.text.toString().trim().isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return false
        }

        return true
    }

    private fun validatePasswordInput(): Boolean {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Current password is required"
            etCurrentPassword.requestFocus()
            return false
        }

        if (newPassword.isEmpty()) {
            etNewPassword.error = "New password is required"
            etNewPassword.requestFocus()
            return false
        }

        if (newPassword.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            etNewPassword.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun updateProfile() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnSaveProfile.isEnabled = false

            val userUpdate = UserUpdate(
                fullName = etFullName.text.toString().trim()
            )

            when (val result = authRepository.updateUser(userId, userUpdate)) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        // Update cached user info
                        tokenManager.saveUserInfo(user.id, user.email, user.fullName, user.role)
                    }
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@EditProfileActivity,
                        result.message ?: "Failed to update profile",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSaveProfile.isEnabled = true
                }
                is Resource.Loading -> {}
            }

            progressBar.visibility = View.GONE
        }
    }

    private fun changePassword() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnChangePassword.isEnabled = false

            val passwordChange = PasswordChange(
                currentPassword = etCurrentPassword.text.toString(),
                newPassword = etNewPassword.text.toString()
            )

            when (val result = authRepository.changePassword(passwordChange)) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Password changed successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Clear password fields
                    etCurrentPassword.setText("")
                    etNewPassword.setText("")
                    etConfirmPassword.setText("")
                    layoutPasswordChange.visibility = View.GONE
                    isPasswordSectionVisible = false
                    btnTogglePasswordChange.text = "Change Password"
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@EditProfileActivity,
                        result.message ?: "Failed to change password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {}
            }

            progressBar.visibility = View.GONE
            btnChangePassword.isEnabled = true
        }
    }
}
