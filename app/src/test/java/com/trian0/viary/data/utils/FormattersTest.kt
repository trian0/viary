package com.trian0.viary.data.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FormattersTest {

    private val ptBr = Locale.forLanguageTag("pt-BR")

    private fun date(year: Int, month: Int, day: Int): Date =
        Calendar.getInstance().apply { set(year, month - 1, day, 12, 0, 0) }.time

    @Test
    fun `money uses the locale separators`() {
        assertEquals("R$ 1.284,50", formatMoney(1284.5, "R$", ptBr))
        assertEquals("$ 1,284.50", formatMoney(1284.5, "$", Locale.US))
    }

    @Test
    fun `money always shows two decimals`() {
        assertEquals("$ 0.00", formatMoney(0.0, "$", Locale.US))
        assertEquals("$ 86.00", formatMoney(86.0, "$", Locale.US))
    }

    @Test
    fun `distance has one decimal and the km unit`() {
        assertEquals("482,3 km", formatDistanceKm(482.3f, ptBr))
        assertEquals("0.0 km", formatDistanceKm(0f, Locale.US))
    }

    @Test
    fun `dates within a month share month and year`() {
        assertEquals("12 – 15 Mar 2026", formatTripDates(date(2026, 3, 12), date(2026, 3, 15), Locale.US))
    }

    @Test
    fun `dates across months show both months`() {
        assertEquals("28 Feb – 2 Mar 2026", formatTripDates(date(2026, 2, 28), date(2026, 3, 2), Locale.US))
    }

    @Test
    fun `dates across years show both years`() {
        assertEquals(
            "28 Dec 2025 – 2 Jan 2026",
            formatTripDates(date(2025, 12, 28), date(2026, 1, 2), Locale.US)
        )
    }

    @Test
    fun `a single day or a missing end shows one date`() {
        assertEquals("12 Mar 2026", formatTripDates(date(2026, 3, 12), date(2026, 3, 12), Locale.US))
        assertEquals("12 Mar 2026", formatTripDates(date(2026, 3, 12), null, Locale.US))
    }

    @Test
    fun `a missing start shows nothing`() {
        assertEquals("", formatTripDates(null, date(2026, 3, 12), Locale.US))
    }
}
