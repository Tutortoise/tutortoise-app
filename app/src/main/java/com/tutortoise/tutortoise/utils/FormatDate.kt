package com.tutortoise.tutortoise.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.component1
import kotlin.collections.component2


enum class SortOrder {
    ASCENDING,  // Oldest first
    DESCENDING  // Newest first (default/current behavior)
}

@RequiresApi(Build.VERSION_CODES.O)
fun groupOrdersByDate(
    orders: List<OrderResponse>,
    sortOrder: SortOrder = SortOrder.DESCENDING
): List<SessionListItem> {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val today = LocalDate.now()
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    return orders
        .groupBy { order ->
            val date = inputFormat.parse(order.sessionTime)
            outputFormat.format(date!!)
        }
        .toSortedMap(if (sortOrder == SortOrder.DESCENDING) reverseOrder() else naturalOrder())
        .flatMap { (_, ordersForDate) ->
            val sessionTimeDate = inputFormat.parse(ordersForDate[0].sessionTime)

            // Check if the session time is today
            val headerDate = if (LocalDate.parse(
                    outputFormat.format(sessionTimeDate!!),
                    DateTimeFormatter.ISO_DATE
                ) == today
            ) {
                "Today"  // Change the header to "Today" if the session is today
            } else {
                isoToReadableDate(ordersForDate[0].sessionTime)  // Use the original date if not today
            }

            listOf(SessionListItem.DateHeader(headerDate)) +
                    ordersForDate
                        .sortedBy { it.sessionTime }
                        .let { list ->
                            if (sortOrder == SortOrder.DESCENDING) list.reversed() else list
                        }
                        .map { SessionListItem.SessionItem(it) }
        }
}

@RequiresApi(Build.VERSION_CODES.O)
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

fun isoToReadableDate(isoDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()

    val date = inputFormat.parse(isoDate)
    return outputFormat.format(date!!).capitalizeFirst()
}

fun String.capitalizeFirst(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}