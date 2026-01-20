package com.example.schoolmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.schoolmanagement.models.Attendance

class AttendanceHistoryAdapter(private val records: List<Attendance>) :
    RecyclerView.Adapter<AttendanceHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvStudentNumber: TextView = view.findViewById(R.id.tvStudentNumber)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = records[position]
        val context = holder.itemView.context

        holder.tvStudentName.text = record.studentName ?: "Student #${record.studentId}"
        holder.tvStudentNumber.text = record.studentNumber ?: "ID: ${record.studentId}"
        holder.tvDate.text = record.date

        // Set status with color coding
        holder.tvStatus.text = record.status.replaceFirstChar { it.uppercase() }
        
        val statusColor = when (record.status.lowercase()) {
            "present" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
            "absent" -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            "late" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            "excused" -> ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            else -> ContextCompat.getColor(context, android.R.color.darker_gray)
        }
        holder.tvStatus.setTextColor(statusColor)

        // Show notes if available
        if (!record.notes.isNullOrEmpty()) {
            holder.tvNotes.visibility = View.VISIBLE
            holder.tvNotes.text = "Notes: ${record.notes}"
        } else {
            holder.tvNotes.visibility = View.GONE
        }
    }

    override fun getItemCount() = records.size
}
