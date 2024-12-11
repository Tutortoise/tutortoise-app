package com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import java.text.NumberFormat

data class RateInfo(
    val averageRate: Float?,
    val location: String,
    val category: String? = null
) {
    fun formatMessage(context: Context): SpannableString {
        if (averageRate == null) {
            return SpannableString("")
        }

        val message: String
        if (averageRate == 0f) {
            message =
                context.getString(R.string.rate_per_hour_first_in_city, location, category)
        } else {
            message = context.getString(
                R.string.rate_per_hour_info_template,
                category ?: "",
                location,
                averageRate.toInt().formatWithThousandsSeparator()
            )
        }

        return SpannableString(message).apply {
            // Make "Info:" bold
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Make the rate amount bold
            val rateString = NumberFormat.getNumberInstance().format(averageRate)
            val rateIndex = message.indexOf(rateString)
            if (rateIndex != -1) {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    rateIndex,
                    rateIndex + rateString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}