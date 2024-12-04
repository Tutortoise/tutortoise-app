package com.tutortoise.tutortoise.presentation.main.learner.reservation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.R


class DateAdapter(
    private val dates: List<String>,
    private val onDateSelected: (String) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition: Int = -1

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateButton: MaterialButton = view.findViewById(R.id.btnDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_date_button, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val position = holder.bindingAdapterPosition
        val date = dates[position]
        holder.dateButton.text = date

        // Set the selected state based on the selected position
        holder.dateButton.isSelected = position == selectedPosition

        holder.dateButton.setOnClickListener {
            if (selectedPosition != position) {
                selectedPosition = position
                notifyDataSetChanged()
                onDateSelected(date)
            }
        }
    }

    override fun getItemCount() = dates.size
}

