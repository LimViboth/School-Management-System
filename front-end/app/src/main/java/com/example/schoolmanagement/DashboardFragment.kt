package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.api.RetrofitClient
import com.example.schoolmanagement.repository.DashboardRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val dashboardRepository by lazy { DashboardRepository() }
    private val tokenManager by lazy { RetrofitClient.getTokenManager() }

    private var tvTotalStudents: TextView? = null
    private var tvTotalClasses: TextView? = null
    private var tvAttendance: TextView? = null
    private var tvGreeting: TextView? = null
    private var tvUserName: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners(view)
        loadDashboardData()
        displayUserInfo()
    }

    private fun initViews(view: View) {
        tvTotalStudents = view.findViewById(R.id.tvTotalStudents)
        tvTotalClasses = view.findViewById(R.id.tvTotalClasses)
        tvAttendance = view.findViewById(R.id.tvAttendance)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun displayUserInfo() {
        val userName = tokenManager.getUserName()
        // Update greeting with user name if available
        tvGreeting?.text = "Hello, ${userName ?: "User"}"
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnViewStudents)?.setOnClickListener {
            navigateToTab(R.id.nav_students)
        }
        
        view.findViewById<Button>(R.id.btnViewClasses)?.setOnClickListener {
            navigateToTab(R.id.nav_classes)
        }
        
        view.findViewById<Button>(R.id.btnViewAttendance)?.setOnClickListener {
            navigateToTab(R.id.nav_attendance)
        }
    }

    private fun navigateToTab(tabId: Int) {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = tabId
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = dashboardRepository.getDashboardStats()) {
                is Resource.Success -> {
                    result.data?.let { stats ->
                        tvTotalStudents?.text = stats.totalStudents.toString()
                        tvTotalClasses?.text = stats.totalClasses.toString()
                        
                        val total = stats.attendanceToday.present + stats.attendanceToday.absent + 
                                   stats.attendanceToday.late + stats.attendanceToday.excused
                        val presentPercentage = if (total > 0) {
                            ((stats.attendanceToday.present.toFloat() / total) * 100).toInt()
                        } else {
                            0
                        }
                        tvAttendance?.text = "$presentPercentage% Present"
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Failed to load dashboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }

            progressBar?.visibility = View.GONE
        }
    }
}
