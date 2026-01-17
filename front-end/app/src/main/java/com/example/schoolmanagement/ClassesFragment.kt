package com.example.schoolmanagement
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class ClassesFragment : Fragment(R.layout.fragment_classes) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerClasses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val classes = listOf(
            SchoolClass("10A", "Mr. Smith", 30),
            SchoolClass("9B", "Ms. Jane", 28)
        )


        recyclerView.adapter    = ClassAdapter(classes)


        view.findViewById<Button>(R.id.btnAddClass).setOnClickListener {
// Open Add Class dialog or screen
        }
    }
}