package com.tutortoise.tutortoise.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateStr: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(dateStr, formatter)

    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    return "${dayName}\n${date.dayOfMonth} ${monthName}"
}

@RequiresApi(Build.VERSION_CODES.O)
fun isoToReadableTime(isoDate: String): String {
    val zonedDateTime = ZonedDateTime.parse(isoDate)
        .withZoneSameInstant(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return zonedDateTime.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun isoToReadableDate(isoDate: String): String {
    val zonedDateTime = ZonedDateTime.parse(isoDate)
        .withZoneSameInstant(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(
        "EEE, d MMM yyyy",
        Locale.getDefault()
    )
    return zonedDateTime.format(formatter).capitalizeFirst()
}

fun String.capitalizeFirst(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}