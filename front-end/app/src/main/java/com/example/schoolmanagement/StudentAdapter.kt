package com.example.schoolmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.models.Student


class StudentAdapter(
    private val students: List<Student>,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {


    inner class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvStudentName)
        val className: TextView = view.findViewById(R.id.tvStudentClass)
        val id: TextView = view.findViewById(R.id.tvStudentId)
        val btnInfo: Button = view.findViewById(R.id.btnInfo)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }


    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.name.text = student.fullName
        holder.className.text = "Class: ${student.className ?: "No Class"}"
        holder.id.text = "ID: ${student.studentId}"

        holder.btnInfo.setOnClickListener { 
            // TODO: Implement student info view
        }
        
        holder.btnEdit.setOnClickListener { 
            onEditClick(student)
        }
        
        holder.btnDelete.setOnClickListener { 
            onDeleteClick(student)
        }
    }


    override fun getItemCount() = students.size
}