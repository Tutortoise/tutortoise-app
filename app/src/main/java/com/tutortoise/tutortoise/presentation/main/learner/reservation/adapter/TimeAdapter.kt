package com.tutortoise.tutortoise.presentation.main.learner.reservation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.R

class TimeAdapter(
    private val times: List<String>,
    private val onTimeSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    private var selectedPosition = -1 // Track the selected position

    class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeButton: MaterialButton = view.findViewById(R.id.btnTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_button, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val position = holder.bindingAdapterPosition
        val time = times[position]
        holder.timeButton.text = time

        holder.timeButton.isSelected = position == selectedPosition

        holder.timeButton.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onTimeSelected(time)
        }
    }

    override fun getItemCount() = times.size
}

