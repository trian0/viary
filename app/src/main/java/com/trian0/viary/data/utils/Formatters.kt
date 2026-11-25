package com.trian0.viary.data.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatMoney(amount: Double, symbol: String, locale: Locale): String {
    val number = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$symbol ${number.format(amount)}"
}

fun formatDistanceKm(km: Float, locale: Locale): String = String.format(locale, "%.1f km", km)

/** "12 – 15 mar 2026", "28 fev – 2 mar 2026" ou "28 dez 2025 – 2 jan 2026", conforme o intervalo. */
fun formatTripDates(start: Date?, end: Date?, locale: Locale): String {
    if (start == null) return ""
    val last = end ?: start

    val first = Calendar.getInstance().apply { time = start }
    val second = Calendar.getInstance().apply { time = last }
    val sameYear = first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
    val sameMonth = sameYear && first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
    val sameDay = sameMonth && first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH)

    val day = SimpleDateFormat("d", locale)
    val month = SimpleDateFormat("MMM", locale)
    val year = SimpleDateFormat("yyyy", locale)
    fun Date.monthName() = month.format(this).trimEnd('.')

    return when {
        sameDay -> "${day.format(start)} ${start.monthName()} ${year.format(start)}"
        sameMonth -> "${day.format(start)} – ${day.format(last)} ${last.monthName()} ${year.format(last)}"
        sameYear -> "${day.format(start)} ${start.monthName()} – ${day.format(last)} ${last.monthName()} ${year.format(last)}"
        else -> "${day.format(start)} ${start.monthName()} ${year.format(start)} – " +
            "${day.format(last)} ${last.monthName()} ${year.format(last)}"
    }
}
