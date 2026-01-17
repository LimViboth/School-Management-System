package com.example.schoolmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
data class SchoolClass(
    val name: String,
    val teacher: String,
    val totalStudents: Int
)


class ClassAdapter(private val classes: List<SchoolClass>) :
    RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {


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


        holder.btnEdit.setOnClickListener { }
        holder.btnDelete.setOnClickListener { }
    }


    override fun getItemCount() = classes.size
}