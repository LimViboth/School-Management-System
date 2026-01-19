package com.example.schoolmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.models.ClassCreate
import com.example.schoolmanagement.models.ClassUpdate
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
            showAddClassDialog()
        }
    }

    private fun showAddClassDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_class, null)
        val etClassName = dialogView.findViewById<EditText>(R.id.etClassName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etGradeLevel = dialogView.findViewById<EditText>(R.id.etGradeLevel)
        val etAcademicYear = dialogView.findViewById<EditText>(R.id.etAcademicYear)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Class")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etClassName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Class name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val classCreate = ClassCreate(
                    name = name,
                    description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() },
                    gradeLevel = etGradeLevel.text.toString().trim().takeIf { it.isNotEmpty() },
                    academicYear = etAcademicYear.text.toString().trim().takeIf { it.isNotEmpty() }
                )
                createClass(classCreate)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createClass(classCreate: ClassCreate) {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            when (val result = classRepository.createClass(classCreate)) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Class created successfully", Toast.LENGTH_SHORT).show()
                    loadClasses()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to create class", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
            progressBar?.visibility = View.GONE
        }
    }

    private fun showEditClassDialog(schoolClass: SchoolClass) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_class, null)
        val etClassName = dialogView.findViewById<EditText>(R.id.etClassName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etGradeLevel = dialogView.findViewById<EditText>(R.id.etGradeLevel)
        val etAcademicYear = dialogView.findViewById<EditText>(R.id.etAcademicYear)

        // Pre-fill with existing data
        etClassName.setText(schoolClass.name)
        etDescription.setText(schoolClass.description ?: "")
        etGradeLevel.setText(schoolClass.gradeLevel ?: "")
        etAcademicYear.setText(schoolClass.academicYear ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Class")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etClassName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Class name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val classUpdate = ClassUpdate(
                    name = name,
                    description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() },
                    gradeLevel = etGradeLevel.text.toString().trim().takeIf { it.isNotEmpty() },
                    academicYear = etAcademicYear.text.toString().trim().takeIf { it.isNotEmpty() }
                )
                updateClass(schoolClass.id, classUpdate)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateClass(classId: Int, classUpdate: ClassUpdate) {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            when (val result = classRepository.updateClass(classId, classUpdate)) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Class updated successfully", Toast.LENGTH_SHORT).show()
                    loadClasses()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to update class", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
            progressBar?.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmation(schoolClass: SchoolClass) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete ${schoolClass.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteClass(schoolClass)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteClass(schoolClass: SchoolClass) {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            when (val result = classRepository.deleteClass(schoolClass.id)) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Class deleted successfully", Toast.LENGTH_SHORT).show()
                    loadClasses()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to delete class", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
            progressBar?.visibility = View.GONE
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
                                id = classModel.id,
                                name = classModel.name,
                                teacher = "Teacher ID: ${classModel.teacherId ?: "N/A"}",
                                totalStudents = classModel.studentCount,
                                description = classModel.description,
                                gradeLevel = classModel.gradeLevel,
                                academicYear = classModel.academicYear,
                                teacherId = classModel.teacherId
                            )
                        }
                        recyclerView?.adapter = ClassAdapter(
                            classList,
                            onEditClick = { schoolClass ->
                                showEditClassDialog(schoolClass)
                            },
                            onDeleteClick = { schoolClass ->
                                showDeleteConfirmation(schoolClass)
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "Failed to load classes",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Show empty state or fallback data
                    recyclerView?.adapter = ClassAdapter(emptyList(), {}, {})
                }
                is Resource.Loading -> {
                    // Already showing loading
                }
            }

            progressBar?.visibility = View.GONE
        }
    }
}