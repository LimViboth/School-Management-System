package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentsFragment : Fragment(R.layout.fragment_students) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerStudents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val students = listOf(
            Student("ST001", "John Doe", "10A"),
            Student("ST002", "Jane Smith", "9B")
        )


        recyclerView.adapter = StudentAdapter(students)
    }
}