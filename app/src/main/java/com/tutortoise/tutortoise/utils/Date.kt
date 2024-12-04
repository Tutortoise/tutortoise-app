package com.tutortoise.tutortoise.utils

import android.annotation.SuppressLint
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
fun groupAvailabilityByDate(availability: List<String>): Map<String, List<String>> {
    // Define the formatter for input and output
    val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
    val outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Convert UTC to local and group by date
    return availability.map { utcTime ->
        val utcDateTime = LocalDateTime.parse(utcTime, inputFormatter)
        val localDateTime =
            utcDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
        localDateTime
    }.groupBy(
        keySelector = { it.toLocalDate().format(outputDateFormatter) }, // Group by date as String
        valueTransform = {
            it.toLocalTime().format(outputTimeFormatter)
        }
    )
}