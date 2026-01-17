package com.example.schoolmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AttendanceStudent(
    val id: String,
    val name: String,
    var isPresent: Boolean = true
)


class AttendanceAdapter(private val students: List<AttendanceStudent>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {


    inner class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvStudentName)
        val id: TextView = view.findViewById(R.id.tvStudentId)
        val present: Switch = view.findViewById(R.id.switchPresent)
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
        holder.present.isChecked = student.isPresent


        holder.present.setOnCheckedChangeListener { _, isChecked ->
            student.isPresent = isChecked
        }
    }


    override fun getItemCount() = students.size
}