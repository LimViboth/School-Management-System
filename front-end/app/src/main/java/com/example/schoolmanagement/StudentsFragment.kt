package com.example.schoolmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudentsFragment : Fragment(R.layout.fragment_students) {

    private val studentRepository by lazy { StudentRepository() }
    
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var etSearch: EditText? = null
    
    private var studentAdapter: StudentAdapter? = null
    private var searchJob: Job? = null
    private var currentSearch: String = ""
    
    private val REQUEST_EDIT_STUDENT = 100

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSearchListener()
        loadStudents()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerStudents)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
    }

    private fun setupSearchListener() {
        etSearch?.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(500) // Debounce delay
                currentSearch = text.toString().trim()
                loadStudents(currentSearch)
            }
        }
    }

    private fun loadStudents(search: String? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = studentRepository.getStudents(search = search)) {
                is Resource.Success -> {
                    result.data?.let { students ->
                        studentAdapter = StudentAdapter(students) { student ->
                            // Handle edit click
                            openEditStudent(student.id)
                        }
                        recyclerView?.adapter = studentAdapter
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Failed to load students",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Show empty state
                    recyclerView?.adapter = StudentAdapter(emptyList()) { }
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }

            progressBar?.visibility = View.GONE
        }
    }

    private fun openEditStudent(studentId: Int) {
        val intent = Intent(requireContext(), EditStudentActivity::class.java)
        intent.putExtra("STUDENT_ID", studentId)
        startActivityForResult(intent, REQUEST_EDIT_STUDENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_STUDENT && resultCode == Activity.RESULT_OK) {
            // Refresh the list after editing
            loadStudents(currentSearch)
        }
    }
}