package com.tutortoise.tutortoise.utils

import android.util.Log
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

fun groupAvailabilityByDate(availability: List<String>): Map<String, List<String>> {
    // Define the formatter for input and output
    val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
    val outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Convert UTC to local and group by date
    return availability.map { utcTime ->
        val utcDateTime = java.time.LocalDateTime.parse(utcTime, inputFormatter)
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

// Not sure if this handle all edge cases
fun generateTimeSlots(
    dayIndex: List<Int>,
    startTime: String,
    endTime: String,
): Map<Int, List<String>> {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
    val end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))

    val localZone = ZoneId.of(TimeZone.getDefault().id) // Get the local zone

    val availability: MutableMap<Int, List<String>> = mutableMapOf()
    for (index in dayIndex) {
        // January 1, 2023 is a Sunday
        val referenceDate = LocalDate.of(2023, Month.JANUARY, 1).plusDays(index.toLong())

        val startDate = referenceDate.atTime(start).atZone(localZone)
        val endDate = referenceDate.atTime(end).atZone(localZone)

        val intervals = mutableListOf<ZonedDateTime>()
        var current = startDate
        do {
            intervals.add(current)
            current = current.plus(Duration.ofMinutes(30))
        } while (current.isBefore(endDate) || current.isEqual(endDate))

        intervals.forEach { interval ->
            // Convert the time to UTC and format it
            val dayIndexInUtc = interval.withZoneSameInstant(ZoneId.of("UTC")).dayOfWeek.value % 7
            val utcTime = interval.withZoneSameInstant(ZoneId.of("UTC"))

            availability[dayIndexInUtc] =
                availability.getOrDefault(dayIndexInUtc, listOf()) + utcTime.format(timeFormatter)
        }
    }

    return availability
}

fun utcAvailabilityToLocal(availability: Map<Int, List<String>>): Map<Int, List<String>> {
    val result = mutableMapOf<Int, List<String>>()

    availability.forEach { (index, times) ->
        // January 1, 2023 is a Sunday
        val referenceDate = LocalDate.of(2023, Month.JANUARY, 1).plusDays(index.toLong())
        for (time in times) {
            val utcTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val localTime = referenceDate.atTime(utcTime).atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())

            val dayIndex = localTime.dayOfWeek.value % 7
            result[dayIndex] = result.getOrDefault(
                dayIndex,
                listOf()
            ) + localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    Log.d("utcAvailabilityToLocal", "Result: $result")
    return result
}