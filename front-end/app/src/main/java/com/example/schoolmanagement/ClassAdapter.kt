package com.example.schoolmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.models.ClassModel

data class SchoolClass(
    val id: Int,
    val name: String,
    val teacher: String,
    val totalStudents: Int,
    val description: String? = null,
    val gradeLevel: String? = null,
    val academicYear: String? = null,
    val teacherId: Int? = null
)


class ClassAdapter(
    private val classes: List<SchoolClass>,
    private val onEditClick: (SchoolClass) -> Unit,
    private val onDeleteClick: (SchoolClass) -> Unit
) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {


    inner class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvClassName)
        val teacher: TextView = view.findViewById(R.id.tvClassTeacher)
        val total: TextView = view.findViewById(R.id.tvClassTotal)
        val btnEdit: Button = view.findViewById(R.id.btnEditClass)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteClass)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }


    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val schoolClass = classes[position]
        holder.name.text = schoolClass.name
        holder.teacher.text = "Teacher: ${schoolClass.teacher}"
        holder.total.text = "Students: ${schoolClass.totalStudents}"


        holder.btnEdit.setOnClickListener { 
            onEditClick(schoolClass)
        }
        holder.btnDelete.setOnClickListener { 
            onDeleteClick(schoolClass)
        }
    }


    override fun getItemCount() = classes.size
}