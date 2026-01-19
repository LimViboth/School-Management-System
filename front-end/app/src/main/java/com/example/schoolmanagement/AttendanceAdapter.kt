package com.example.schoolmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AttendanceStudent(
    val id: String,
    val name: String,
    var status: String = "present" // "present", "absent", or "excused"
)


class AttendanceAdapter(private val students: List<AttendanceStudent>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {


    inner class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvStudentName)
        val id: TextView = view.findViewById(R.id.tvStudentId)
        val radioGroupStatus: RadioGroup = view.findViewById(R.id.radioGroupStatus)
        val radioPresent: RadioButton = view.findViewById(R.id.radioPresent)
        val radioAbsent: RadioButton = view.findViewById(R.id.radioAbsent)
        val radioExcused: RadioButton = view.findViewById(R.id.radioExcused)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }


    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        holder.name.text = student.name
        holder.id.text = student.id
        
        // Set initial selection based on status
        when (student.status) {
            "present" -> holder.radioPresent.isChecked = true
            "absent" -> holder.radioAbsent.isChecked = true
            "excused" -> holder.radioExcused.isChecked = true
            else -> holder.radioPresent.isChecked = true
        }

        // Listen for status changes
        holder.radioGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            student.status = when (checkedId) {
                R.id.radioPresent -> "present"
                R.id.radioAbsent -> "absent"
                R.id.radioExcused -> "excused"
                else -> "present"
            }
        }
    }


    override fun getItemCount() = students.size
}