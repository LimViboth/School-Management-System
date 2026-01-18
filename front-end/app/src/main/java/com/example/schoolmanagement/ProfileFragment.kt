package com.example.schoolmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val authRepository by lazy { AuthRepository() }
    private val tokenManager by lazy { RetrofitClient.getTokenManager() }

    private var tvUserName: TextView? = null
    private var tvUserEmail: TextView? = null
    private var tvUserRole: TextView? = null
    private var btnLogout: LinearLayout? = null
    private var progressBar: ProgressBar? = null

    private val REQUEST_EDIT_PROFILE = 101

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        displayUserInfo()
        setupClickListeners(view)
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserRole = view.findViewById(R.id.tvUserRole)
        btnLogout = view.findViewById(R.id.btnLogout)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun displayUserInfo() {
        // Display cached user info
        val (name, email, role) = authRepository.getUserInfo()
        tvUserName?.text = name ?: "User"
        tvUserEmail?.text = email ?: ""
        tvUserRole?.text = role?.replaceFirstChar { it.uppercase() } ?: "Teacher"

        // Also fetch fresh data from API
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = authRepository.getCurrentUser()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        tvUserName?.text = user.fullName
                        tvUserEmail?.text = user.email
                        tvUserRole?.text = user.role.replaceFirstChar { it.uppercase() }

                        // Update cached info
                        tokenManager.saveUserInfo(user.id, user.email, user.fullName, user.role)
                    }
                }

                is Resource.Error -> {
                    // Use cached data if API fails, already displayed
                }

                is Resource.Loading -> {}
            }

            progressBar?.visibility = View.GONE
        }
    }

    private fun setupClickListeners(view: View) {
        btnLogout?.setOnClickListener {
            logout()
        }

        view.findViewById<Button>(R.id.btnEditProfile)?.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivityForResult(intent, REQUEST_EDIT_PROFILE)
        }
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (authRepository.logout()) {
                is Resource.Success, is Resource.Error -> {
                    // Navigate to login regardless of API response
                    navigateToLogin()
                }

                is Resource.Loading -> {}
            }

            progressBar?.visibility = View.GONE
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // Refresh profile data after editing
            displayUserInfo()
        }
    }
}
