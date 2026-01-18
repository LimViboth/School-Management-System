package com.example.schoolmanagement

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.schoolmanagement.api.Resource
import com.example.schoolmanagement.models.StudentUpdate
import com.example.schoolmanagement.repository.StudentRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditStudentActivity : AppCompatActivity() {

    private val studentRepository by lazy { StudentRepository() }
    
    private lateinit var etStudentId: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etAddress: EditText
    private lateinit var etParentName: EditText
    private lateinit var etParentPhone: EditText
    private lateinit var etParentEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    
    private var studentId: Int = 0
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_student)

        studentId = intent.getIntExtra("STUDENT_ID", 0)
        
        if (studentId == 0) {
            Toast.makeText(this, "Error: Invalid student ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupGenderSpinner()
        setupDatePicker()
        loadStudentData()
        setupClickListeners()
    }

    private fun initViews() {
        etStudentId = findViewById(R.id.etStudentId)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        spinnerGender = findViewById(R.id.spinnerGender)
        etAddress = findViewById(R.id.etAddress)
        etParentName = findViewById(R.id.etParentName)
        etParentPhone = findViewById(R.id.etParentPhone)
        etParentEmail = findViewById(R.id.etParentEmail)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)

        // Student ID should not be editable
        etStudentId.isEnabled = false
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter
    }

    private fun setupDatePicker() {
        etDateOfBirth.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etDateOfBirth.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadStudentData() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            when (val result = studentRepository.getStudent(studentId)) {
                is Resource.Success -> {
                    result.data?.let { student ->
                        etStudentId.setText(student.studentId)
                        etFirstName.setText(student.firstName)
                        etLastName.setText(student.lastName)
                        etEmail.setText(student.email)
                        etPhone.setText(student.phone)
                        etDateOfBirth.setText(student.dateOfBirth)
                        etAddress.setText(student.address)
                        etParentName.setText(student.parentName)
                        etParentPhone.setText(student.parentPhone)
                        etParentEmail.setText(student.parentEmail)
                        
                        // Set gender spinner
                        when (student.gender?.lowercase()) {
                            "male" -> spinnerGender.setSelection(1)
                            "female" -> spinnerGender.setSelection(2)
                            "other" -> spinnerGender.setSelection(3)
                            else -> spinnerGender.setSelection(0)
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@EditStudentActivity,
                        result.message ?: "Failed to load student data",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is Resource.Loading -> {}
            }
            
            progressBar.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInput()) {
                updateStudent()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (etFirstName.text.toString().trim().isEmpty()) {
            etFirstName.error = "First name is required"
            etFirstName.requestFocus()
            return false
        }

        if (etLastName.text.toString().trim().isEmpty()) {
            etLastName.error = "Last name is required"
            etLastName.requestFocus()
            return false
        }

        val email = etEmail.text.toString().trim()
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email address"
            etEmail.requestFocus()
            return false
        }

        val parentEmail = etParentEmail.text.toString().trim()
        if (parentEmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(parentEmail).matches()) {
            etParentEmail.error = "Invalid parent email address"
            etParentEmail.requestFocus()
            return false
        }

        return true
    }

    private fun updateStudent() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false

            val gender = when (spinnerGender.selectedItemPosition) {
                1 -> "Male"
                2 -> "Female"
                3 -> "Other"
                else -> null
            }

            val studentUpdate = StudentUpdate(
                firstName = etFirstName.text.toString().trim(),
                lastName = etLastName.text.toString().trim(),
                email = etEmail.text.toString().trim().takeIf { it.isNotEmpty() },
                phone = etPhone.text.toString().trim().takeIf { it.isNotEmpty() },
                dateOfBirth = etDateOfBirth.text.toString().trim().takeIf { it.isNotEmpty() },
                gender = gender,
                address = etAddress.text.toString().trim().takeIf { it.isNotEmpty() },
                parentName = etParentName.text.toString().trim().takeIf { it.isNotEmpty() },
                parentPhone = etParentPhone.text.toString().trim().takeIf { it.isNotEmpty() },
                parentEmail = etParentEmail.text.toString().trim().takeIf { it.isNotEmpty() }
            )

            when (val result = studentRepository.updateStudent(studentId, studentUpdate)) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@EditStudentActivity,
                        "Student updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@EditStudentActivity,
                        result.message ?: "Failed to update student",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSave.isEnabled = true
                }
                is Resource.Loading -> {}
            }

            progressBar.visibility = View.GONE
        }
    }
}
