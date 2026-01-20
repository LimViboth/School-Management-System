package com.example.schoolmanagement

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.models.Attendance
import com.example.schoolmanagement.repository.AttendanceRepository
import com.example.schoolmanagement.repository.ClassRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceHistoryActivity : AppCompatActivity() {

    private val attendanceRepository by lazy { AttendanceRepository() }
    private val classRepository by lazy { ClassRepository() }

    private lateinit var spinnerClass: Spinner
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSelectDate: Button
    private lateinit var btnSearch: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoRecords: TextView
    private lateinit var btnBack: ImageButton

    private val classIdMap = mutableMapOf<String, Int>()
    private var selectedClassId: Int? = null
    private var selectedDate: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_history)

        initViews()
        setupClickListeners()
        loadClasses()
        
        // Set today's date as default
        selectedDate = dateFormat.format(calendar.time)
        tvSelectedDate.text = displayDateFormat.format(calendar.time)
    }

    private fun initViews() {
        spinnerClass = findViewById(R.id.spinnerClass)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerHistory)
        progressBar = findViewById(R.id.progressBar)
        tvNoRecords = findViewById(R.id.tvNoRecords)
        btnBack = findViewById(R.id.btnBack)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSearch.setOnClickListener {
            searchAttendance()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
                tvSelectedDate.text = displayDateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadClasses() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            when (val result = classRepository.getClasses()) {
                is Resource.Success -> {
                    result.data?.let { classes ->
                        val classNames = mutableListOf("Select a class")
                        classIdMap.clear()

                        classes.forEach { classModel ->
                            classNames.add(classModel.name)
                            classIdMap[classModel.name] = classModel.id
                        }

                        spinnerClass.adapter = ArrayAdapter(
                            this@AttendanceHistoryActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            classNames
                        )

                        spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                if (position > 0) {
                                    val className = classNames[position]
                                    selectedClassId = classIdMap[className]
                                } else {
                                    selectedClassId = null
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedClassId = null
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this@AttendanceHistoryActivity, result.message ?: "Failed to load classes", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }

            progressBar.visibility = View.GONE
        }
    }

    private fun searchAttendance() {
        val classId = selectedClassId
        val date = selectedDate

        if (classId == null) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show()
            return
        }

        if (date == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            tvNoRecords.visibility = View.GONE

            when (val result = attendanceRepository.getAttendance(classId, date)) {
                is Resource.Success -> {
                    result.data?.let { records ->
                        if (records.isEmpty()) {
                            tvNoRecords.visibility = View.VISIBLE
                            recyclerView.adapter = AttendanceHistoryAdapter(emptyList())
                        } else {
                            tvNoRecords.visibility = View.GONE
                            recyclerView.adapter = AttendanceHistoryAdapter(records)
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this@AttendanceHistoryActivity, result.message ?: "Failed to load attendance", Toast.LENGTH_SHORT).show()
                    tvNoRecords.visibility = View.VISIBLE
                    tvNoRecords.text = "Error loading records"
                }
                is Resource.Loading -> {}
            }

            progressBar.visibility = View.GONE
        }
    }

    companion object {
        fun newIntent(context: android.content.Context): Intent {
            return Intent(context, AttendanceHistoryActivity::class.java)
        }
    }
}
