package com.tutortoise.tutortoise.presentation.main.tutor.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
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
    private var previousSelectedView: View? = null
    private val today: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }


    inner class CalendarViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private fun animateSelection(view: View) {
            val selectAnim = android.view.animation.AnimationUtils.loadAnimation(
                view.context,
                R.anim.calendar_item_select
            )
            view.startAnimation(selectAnim)
        }

        private fun animateDeselection(view: View) {
            val deselectAnim = android.view.animation.AnimationUtils.loadAnimation(
                view.context,
                R.anim.calendar_item_deselect
            )
            view.startAnimation(deselectAnim)
        }

        private fun animateBackgroundChange(view: View, newBackground: Int?) {
            val fadeOut = android.view.animation.AnimationUtils.loadAnimation(
                view.context,
                R.anim.bg_fade_out
            )

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    if (newBackground != null) {
                        view.setBackgroundResource(newBackground)
                        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(
                            view.context,
                            R.anim.bg_fade_in
                        )
                        view.startAnimation(fadeIn)
                    } else {
                        view.background = null
                    }
                }
            })

            view.startAnimation(fadeOut)
        }

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

                root.isClickable = date.isCurrentMonth
                root.setOnClickListener {
                    if (date.isCurrentMonth) {
                        previousSelectedView?.let { prevView ->
                            if (prevView != root) {
                                animateDeselection(prevView)
                            }
                        }

                        animateSelection(root)
                        previousSelectedView = root

                        setSelectedDate(date.calendar)
                        onDateSelected(date.calendar)
                    }
                }

                if (isDateSelected) {
                    previousSelectedView = root
                }

                val newBackground = when {
                    isDateSelected && isToday -> R.drawable.bg_current_date
                    isDateSelected -> R.drawable.bg_selected_date
                    isToday && selectedDate == null -> R.drawable.bg_current_date
                    else -> null
                }

                if (root.background == null && newBackground != null) {
                    root.setBackgroundResource(newBackground)
                    val fadeIn = android.view.animation.AnimationUtils.loadAnimation(
                        root.context,
                        R.anim.bg_fade_in
                    )
                    root.startAnimation(fadeIn)
                } else if (root.background != null && newBackground == null) {
                    animateBackgroundChange(root, null)
                } else if (root.background != null && newBackground != null) {
                    animateBackgroundChange(root, newBackground)
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
