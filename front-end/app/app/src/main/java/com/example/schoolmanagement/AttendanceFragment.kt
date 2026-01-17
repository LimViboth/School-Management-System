package com.example.schoolmanagement

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AttendanceFragment : Fragment(R.layout.fragment_attendance) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerAttendance)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val students = listOf(
            AttendanceStudent("ST001", "John Doe"),
            AttendanceStudent("ST002", "Jane Smith")
        )


        recyclerView.adapter = AttendanceAdapter(students)


        view.findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
// Open attendance history screen
        }
    }
}