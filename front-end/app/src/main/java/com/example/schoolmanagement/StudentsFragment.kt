package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.repository.StudentRepository
import kotlinx.coroutines.launch

class StudentsFragment : Fragment(R.layout.fragment_students) {

    private val studentRepository by lazy { StudentRepository() }
    
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadStudents()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerStudents)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun loadStudents() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = studentRepository.getStudents()) {
                is Resource.Success -> {
                    result.data?.let { students ->
                        // Convert API models to local Student model for adapter
                        val studentList = students.map { student ->
                            Student(
                                id = student.studentId,
                                name = student.fullName,
                                className = student.className ?: "No Class"
                            )
                        }
                        recyclerView?.adapter = StudentAdapter(studentList)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Failed to load students",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Show empty state or fallback data
                    recyclerView?.adapter = StudentAdapter(emptyList())
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }

            progressBar?.visibility = View.GONE
        }
    }
}