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
    private var dates: List<CalendarDate> = emptyList(),
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    data class CalendarDate(
        val calendar: Calendar,
        val isCurrentMonth: Boolean = true,
        val hasSchedule: Boolean = false,
        val isSelected: Boolean = false,
        val isToday: Boolean = false
    ) {
        fun isSameDay(other: Calendar): Boolean {
            return calendar.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
        }
    }

    private var selectedDate: Calendar? = null
    private val today: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }


    inner class CalendarViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: CalendarDate) {
            binding.apply {
                tvDate.text = date.calendar.get(Calendar.DAY_OF_MONTH).toString()

                val isToday = date.calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        date.calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        date.calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                val isDateSelected = selectedDate?.let { selected ->
                    date.isSameDay(selected)
                } == true

                tvDate.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        when {
                            isDateSelected -> R.color.white // Selected date is always white
                            isToday && selectedDate == null -> R.color.white // Today is white only when no date is selected
                            date.isCurrentMonth -> R.color.black // Current month dates are black
                            else -> R.color.colorInactive // Other month dates are inactive
                        }
                    )
                )

                dotIndicator.visibility =
                    if (date.hasSchedule && !isDateSelected && !(isToday && selectedDate == null)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                when {
                    isDateSelected && isToday -> {
                        root.setBackgroundResource(R.drawable.bg_current_date)
                    }

                    isDateSelected -> {
                        root.setBackgroundResource(R.drawable.bg_selected_date)
                    }

                    isToday && selectedDate == null -> {
                        root.setBackgroundResource(R.drawable.bg_current_date)
                    }

                    else -> {
                        root.background = null
                    }
                }

                root.isClickable = date.isCurrentMonth
                root.setOnClickListener {
                    if (date.isCurrentMonth) {
                        setSelectedDate(date.calendar)
                        onDateSelected(date.calendar)
                    }
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

    fun updateDates(newDates: List<Calendar>, scheduledDates: Set<Calendar>) {
        dates = newDates.map { calendar ->
            val normalizedCalendar = calendar.clone() as Calendar
            normalizedCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            CalendarDate(
                calendar = normalizedCalendar,
                isCurrentMonth = calendar.get(Calendar.MONTH) == newDates[15].get(Calendar.MONTH),
                hasSchedule = scheduledDates.any { scheduled ->
                    normalizedCalendar.get(Calendar.YEAR) == scheduled.get(Calendar.YEAR) &&
                            normalizedCalendar.get(Calendar.MONTH) == scheduled.get(Calendar.MONTH) &&
                            normalizedCalendar.get(Calendar.DAY_OF_MONTH) == scheduled.get(Calendar.DAY_OF_MONTH)
                },
                isToday = normalizedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        normalizedCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        normalizedCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
            )
        }
        notifyDataSetChanged()
    }


    fun setSelectedDate(date: Calendar?) {
        selectedDate = date?.clone() as? Calendar
        selectedDate?.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        notifyDataSetChanged()
    }
}
