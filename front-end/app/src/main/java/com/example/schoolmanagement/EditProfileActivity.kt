package com.example.schoolmanagement

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.models.PasswordChange
import com.example.schoolmanagement.models.UserUpdate
import com.example.schoolmanagement.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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
    private var selectedProfilePictureBase64: String? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

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
                fullName = etFullName.text.toString().trim(),
                profilePicture = selectedProfilePictureBase64
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

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkPermissionAndPickImage()
                    1 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize bitmap to reduce size
            val resizedBitmap = resizeBitmap(bitmap, 500)

            // Convert to Base64
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            selectedProfilePictureBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)

            Toast.makeText(this, "Profile picture selected", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (maxSize / ratio).toInt()
        } else {
            height = maxSize
            width = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
