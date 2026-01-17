package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.repository.ClassRepository
import kotlinx.coroutines.launch

class ClassesFragment : Fragment(R.layout.fragment_classes) {

    private val classRepository by lazy { ClassRepository() }
    
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners(view)
        loadClasses()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerClasses)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnAddClass)?.setOnClickListener {
            // Open Add Class dialog or screen
            Toast.makeText(requireContext(), "Add Class feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadClasses() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = classRepository.getClasses()) {
                is Resource.Success -> {
                    result.data?.let { classes ->
                        // Convert API models to local SchoolClass model for adapter
                        val classList = classes.map { classModel ->
                            SchoolClass(
                                name = classModel.name,
                                teacher = "Teacher ID: ${classModel.teacherId ?: "N/A"}",
                                totalStudents = classModel.studentCount
                            )
                        }
                        recyclerView?.adapter = ClassAdapter(classList)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Failed to load classes",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Show empty state or fallback data
                    recyclerView?.adapter = ClassAdapter(emptyList())
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }

            progressBar?.visibility = View.GONE
        }
    }
}