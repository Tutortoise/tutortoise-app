package com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories

import java.text.NumberFormat

fun Number.formatWithThousandsSeparator(): String {
    return NumberFormat.getNumberInstance().format(this).replace(",", ".")
}

fun String.parseFormattedNumber(): Long {
    return replace(".", "").toLongOrNull() ?: 0
}