package com.example.schoolmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.models.AttendanceRecord
import com.example.schoolmanagement.repository.AttendanceRepository
import com.example.schoolmanagement.repository.ClassRepository
import com.example.schoolmanagement.repository.StudentRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment(R.layout.fragment_attendance) {

    private val attendanceRepository by lazy { AttendanceRepository() }
    private val studentRepository by lazy { StudentRepository() }
    private val classRepository by lazy { ClassRepository() }
    
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var btnSaveAttendance: Button? = null
    private var spinnerClass: Spinner? = null
    
    private var currentClassId: Int? = null
    private var attendanceStudents = mutableListOf<AttendanceStudent>()
    private val classIdMap = mutableMapOf<String, Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners(view)
        loadClasses()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerAttendance)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        progressBar = view.findViewById(R.id.progressBar)
        btnSaveAttendance = view.findViewById(R.id.btnSaveAttendance)
        spinnerClass = view.findViewById(R.id.spinnerClass)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnViewHistory)?.setOnClickListener {
            startActivity(AttendanceHistoryActivity.newIntent(requireContext()))
        }
        
        btnSaveAttendance?.setOnClickListener {
            saveAttendance()
        }
    }

    private fun loadClasses() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = classRepository.getClasses()) {
                is Resource.Success -> {
                    result.data?.let { classes ->
                        val classNames = mutableListOf("Select a class")
                        classIdMap.clear()
                        
                        classes.forEach { classModel ->
                            classNames.add(classModel.name)
                            classIdMap[classModel.name] = classModel.id
                        }
                        
                        spinnerClass?.adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            classNames
                        )
                        
                        spinnerClass?.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                                if (position > 0) {
                                    val className = classNames[position]
                                    currentClassId = classIdMap[className]
                                    currentClassId?.let { loadStudentsForClass(it) }
                                }
                            }
                            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                        })
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to load classes", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }

            progressBar?.visibility = View.GONE
        }
    }

    private fun loadStudentsForClass(classId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            when (val result = studentRepository.getStudents(classId = classId)) {
                is Resource.Success -> {
                    result.data?.let { students ->
                        attendanceStudents = students.map { student ->
                            AttendanceStudent(
                                id = student.id.toString(),
                                name = student.fullName,
                                status = "present"
                            )
                        }.toMutableList()
                        recyclerView?.adapter = AttendanceAdapter(attendanceStudents)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to load students", Toast.LENGTH_SHORT).show()
                    recyclerView?.adapter = AttendanceAdapter(emptyList())
                }
                is Resource.Loading -> {}
            }

            progressBar?.visibility = View.GONE
        }
    }

    private fun saveAttendance() {
        val classId = currentClassId
        if (classId == null) {
            Toast.makeText(requireContext(), "Please select a class first", Toast.LENGTH_SHORT).show()
            return
        }

        if (attendanceStudents.isEmpty()) {
            Toast.makeText(requireContext(), "No students to mark attendance for", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val records = attendanceStudents.map { student ->
                AttendanceRecord(
                    studentId = student.id.toIntOrNull() ?: 0,
                    status = student.status
                )
            }

            when (val result = attendanceRepository.createBulkAttendance(classId, today, records)) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to save attendance", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }

            progressBar?.visibility = View.GONE
        }
    }
}