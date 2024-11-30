package com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.tutortoise.tutortoise.R
import java.text.NumberFormat

data class RateInfo(
    val averageRate: Int,
    val location: String,
    val subject: String? = null
) {
    fun formatMessage(context: Context): SpannableString {
        val message = context.getString(
            R.string.rate_per_hour_info_template,
            subject ?: "",
            location,
            NumberFormat.getNumberInstance().format(averageRate)
        )

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