package com.tutortoise.tutortoise.presentation.main.tutor.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ItemCalendarDayBinding
import java.util.Calendar

class CalendarAdapter(
    private var dates: List<CalendarDate>,
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedDate: Calendar = Calendar.getInstance()
    private val today: Calendar = Calendar.getInstance()

    data class CalendarDate(
        val calendar: Calendar,
        val isCurrentMonth: Boolean = true,
        val hasSchedule: Boolean = false,
        val isSelected: Boolean = false,
        val isToday: Boolean = false
    )

    inner class CalendarViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(date: CalendarDate) {
            binding.apply {
                tvDate.text = date.calendar.get(Calendar.DAY_OF_MONTH).toString()

                tvDate.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        when {
                            date.isToday -> R.color.white
                            date.isCurrentMonth -> R.color.black
                            else -> R.color.colorInactive
                        }
                    )
                )

                dotIndicator.visibility =
                    if (date.hasSchedule && !date.isToday) View.VISIBLE else View.GONE

                when {
                    date.isToday -> {
                        root.setBackgroundResource(R.drawable.bg_current_date)
                    }

                    date.isSelected -> {
                        root.setBackgroundResource(R.drawable.bg_selected_date)
                    }

                    else -> {
                        root.setBackgroundResource(android.R.color.transparent)
                    }
                }

                root.setOnClickListener {
                    selectedDate = date.calendar
                    onDateSelected(date.calendar)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return CalendarViewHolder(
            ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount(): Int = dates.size

    fun updateDates(newDates: List<Calendar>, scheduledDates: Set<Calendar> = emptySet()) {
        dates = newDates.map { calendar ->
            CalendarDate(
                calendar = calendar,
                isCurrentMonth = calendar.get(Calendar.MONTH) == newDates[15].get(Calendar.MONTH),
                hasSchedule = scheduledDates.any { scheduled ->
                    calendar.get(Calendar.YEAR) == scheduled.get(Calendar.YEAR) &&
                            calendar.get(Calendar.MONTH) == scheduled.get(Calendar.MONTH) &&
                            calendar.get(Calendar.DAY_OF_MONTH) == scheduled.get(Calendar.DAY_OF_MONTH)
                },
                isSelected = calendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH),
                isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
            )
        }
        notifyDataSetChanged()
    }
}
