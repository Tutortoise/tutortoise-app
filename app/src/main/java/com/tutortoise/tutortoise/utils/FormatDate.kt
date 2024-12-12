package com.tutortoise.tutortoise.utils

import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.component1
import kotlin.collections.component2


enum class SortOrder {
    ASCENDING,  // Oldest first
    DESCENDING  // Newest first (default/current behavior)
}

fun groupOrdersByDate(
    orders: List<OrderResponse>,
    sortOrder: SortOrder = SortOrder.DESCENDING,
    groupByField: (OrderResponse) -> String = { it.sessionTime } // Default to sessionTime
): List<SessionListItem> {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val today = LocalDate.now()
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    return orders
        .groupBy { order ->
            val date = inputFormat.parse(groupByField(order))
            outputFormat.format(date!!)
        }
        .toSortedMap(if (sortOrder == SortOrder.DESCENDING) reverseOrder() else naturalOrder())
        .flatMap { (_, ordersForDate) ->
            val firstDate = inputFormat.parse(groupByField(ordersForDate[0]))

            // Check if the date is today
            val headerDate = if (LocalDate.parse(
                    outputFormat.format(firstDate!!),
                    DateTimeFormatter.ISO_DATE
                ) == today
            ) {
                "Today"  // Change the header to "Today" if the date is today
            } else {
                isoToReadableDate(groupByField(ordersForDate[0]))  // Use the original date if not today
            }

            listOf(SessionListItem.DateHeader(headerDate)) +
                    ordersForDate
                        .sortedBy { groupByField(it) }
                        .let { list ->
                            if (sortOrder == SortOrder.DESCENDING) list.reversed() else list
                        }
                        .map { SessionListItem.SessionItem(it) }
        }
}

fun formatDate(dateStr: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(dateStr, formatter)

    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    return "${dayName}\n${date.dayOfMonth} $monthName"
}

fun isoToReadableTime(isoDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    val date = inputFormat.parse(isoDate)
    return outputFormat.format(date!!)
}

fun isoToReadableTimeRange(isoDate: String, totalHours: Int): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    val date = inputFormat.parse(isoDate) ?: throw IllegalArgumentException("Invalid date format")

    val startTime = outputFormat.format(date)

    // Calculate the end time
    val calendar = Calendar.getInstance().apply {
        time = date
        add(Calendar.HOUR_OF_DAY, totalHours)
    }
    val endTime = outputFormat.format(calendar.time)

    // Return the formatted time range
    return "$startTime - $endTime"
}

fun isoToReadableDate(isoDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    val date = inputFormat.parse(isoDate)
    return outputFormat.format(date!!).capitalizeFirst()
}

fun isoToRelativeDate(isoDate: String): String {
    val instant = Instant.parse(isoDate)
    val now = Instant.now()
    val duration = Duration.between(instant, now)

    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toHours() < 1 -> "${duration.toMinutes()} minutes ago"
        duration.toDays() < 1 -> "${duration.toHours()} hours ago"
        duration.toDays() < 30 -> "${duration.toDays()} days ago"
        duration.toDays() < 365 -> {
            val months = duration.toDays() / 30
            "$months month${if (months > 1) "s" else ""} ago"
        }

        else -> {
            val years = duration.toDays() / 365
            "$years year${if (years > 1) "s" else ""} ago"
        }
    }
}

fun String.capitalizeFirst(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}