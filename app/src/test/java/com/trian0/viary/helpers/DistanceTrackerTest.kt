package com.trian0.viary.helpers

import android.location.Location
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DistanceTrackerTest {

    private var meters = 0f
    private val tracker = DistanceTracker { _, _ -> meters }
    private val empty = DistanceTracker.State(lastLocation = null, totalDistanceKm = 0f)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun fakeLocation(accuracy: Float = 5f, elapsedSeconds: Long = 0): Location {
        val location = mockk<Location>(relaxed = true)
        every { location.hasAccuracy() } returns true
        every { location.accuracy } returns accuracy
        every { location.elapsedRealtimeNanos } returns elapsedSeconds * 1_000_000_000L
        return location
    }

    @Test
    fun `primeira leitura apenas fixa a referencia, sem somar distancia`() {
        meters = 1000f

        val next = tracker.next(empty, fakeLocation())

        assertEquals(0f, next.totalDistanceKm)
    }

    @Test
    fun `segunda leitura com deslocamento real soma a distancia`() {
        val first = tracker.next(empty, fakeLocation(elapsedSeconds = 0))

        meters = 1000f
        val second = tracker.next(first, fakeLocation(elapsedSeconds = 100))

        assertEquals(1f, second.totalDistanceKm)
    }

    @Test
    fun `leitura com baixa acuracia e ignorada`() {
        val first = tracker.next(empty, fakeLocation())

        meters = 1000f
        val second = tracker.next(first, fakeLocation(accuracy = 999f))

        assertSame(first, second)
    }

    @Test
    fun `deslocamento abaixo do limiar minimo e ignorado`() {
        val reference = fakeLocation()
        val first = tracker.next(empty, reference)

        meters = 3f
        val second = tracker.next(first, fakeLocation())

        assertEquals(0f, second.totalDistanceKm)
        assertSame(reference, second.lastLocation)
    }

    @Test
    fun `limiar sobe junto com a imprecisao da leitura`() {
        val first = tracker.next(empty, fakeLocation())

        meters = 20f
        val second = tracker.next(first, fakeLocation(accuracy = 25f))

        assertEquals(0f, second.totalDistanceKm)
    }

    @Test
    fun `salto de GPS incompativel com a velocidade e ignorado`() {
        val first = tracker.next(empty, fakeLocation(elapsedSeconds = 0))

        meters = 5000f // 5km em 2s
        val second = tracker.next(first, fakeLocation(elapsedSeconds = 2))

        assertEquals(0f, second.totalDistanceKm)
        assertSame(first.lastLocation, second.lastLocation)
    }

    @Test
    fun `deslocamento rapido e continuo nao trava o rastreio`() {
        var state = tracker.next(empty, fakeLocation(elapsedSeconds = 0))

        // 150 m/s sustentado (acima do limite): a referencia fica parada ate o 5o salto seguido
        repeat(DistanceTracker.MAX_CONSECUTIVE_JUMPS) { i ->
            val seconds = (i + 1).toLong()
            meters = 150f * seconds
            state = tracker.next(state, fakeLocation(elapsedSeconds = seconds))
        }

        assertEquals(0.75f, state.totalDistanceKm)
        assertEquals(0, state.rejectedJumps)
    }

    @Test
    fun `leitura normal zera a contagem de saltos`() {
        val first = tracker.next(empty, fakeLocation(elapsedSeconds = 0))
        meters = 5000f
        val afterJump = tracker.next(first, fakeLocation(elapsedSeconds = 2))

        meters = 1f
        val afterNormal = tracker.next(afterJump, fakeLocation(elapsedSeconds = 3))

        assertEquals(0, afterNormal.rejectedJumps)
    }

    @Test
    fun `mesma distancia com tempo plausivel e aceita apos um salto ignorado`() {
        val first = tracker.next(empty, fakeLocation(elapsedSeconds = 0))
        meters = 5000f
        val afterJump = tracker.next(first, fakeLocation(elapsedSeconds = 2))

        // referencia nao andou; 5km em 10min (~8m/s) e deslocamento real
        val afterGap = tracker.next(afterJump, fakeLocation(elapsedSeconds = 600))

        assertEquals(5f, afterGap.totalDistanceKm)
    }
}
