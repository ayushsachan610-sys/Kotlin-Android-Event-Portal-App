package com.zenith26.fest.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateHelper {

    fun formatIsoTimestamp(isoString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            val date = isoFormat.parse(isoString.substringBefore("."))
            if (date != null) {
                val outputFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                outputFormat.format(date)
            } else {
                isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
