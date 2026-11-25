package com.trian0.viary.ui.viarydetails.share

import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.GeoPoint
import com.trian0.viary.data.models.Viary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareMapDataTest {

    private val origin = GeoPoint(-22.9, -47.0)
    private val checkpoint = GeoPoint(-22.7, -46.8)
    private val destination = GeoPoint(-22.5, -45.6)
    private val route = listOf(origin, GeoPoint(-22.8, -46.9), destination)

    @Test
    fun `line follows the route when there is one`() {
        val data = ShareMapData(origin, listOf(checkpoint), destination, route)

        assertEquals(route, data.line)
    }

    @Test
    fun `line connects the markers in order when the route is empty`() {
        val data = ShareMapData(origin, listOf(checkpoint), destination, emptyList())

        assertEquals(listOf(origin, checkpoint, destination), data.line)
    }

    @Test
    fun `line skips a missing origin or destination`() {
        val data = ShareMapData(null, listOf(checkpoint), null, emptyList())

        assertEquals(listOf(checkpoint), data.line)
    }

    @Test
    fun `line is empty without any point`() {
        assertTrue(ShareMapData(null, emptyList(), null, emptyList()).line.isEmpty())
    }

    private fun viary(latOrigin: Double = 0.0, lonOrigin: Double = 0.0, latEnd: Double = 0.0, lonEnd: Double = 0.0) =
        Viary(
            name = "Viagem",
            origin = "Campinas",
            departureTime = null,
            status = Viary.ViaryStatus.COMPLETED,
            kmEnd = 10f,
            selectedImage = null,
            climate = null,
            latitudeOrigin = latOrigin,
            longitudeOrigin = lonOrigin,
            latitudeArrival = latEnd,
            longitudeArrival = lonEnd,
        )

    @Test
    fun `build keeps only the points that have coordinates`() {
        val withCoordinates = Checkpoint(latitude = checkpoint.latitude, longitude = checkpoint.longitude)
        val withoutCoordinates = Checkpoint()

        val data = buildShareMapData(
            viary(origin.latitude, origin.longitude),
            listOf(withCoordinates, withoutCoordinates),
            route,
        )

        assertEquals(origin, data.origin)
        assertEquals(listOf(checkpoint), data.checkpoints)
        assertNull(data.destination)
        assertEquals(route, data.route)
    }

    @Test
    fun `build sets the destination when the arrival was recorded`() {
        val data = buildShareMapData(viary(latEnd = destination.latitude, lonEnd = destination.longitude), emptyList(), emptyList())

        assertEquals(destination, data.destination)
        assertNull(data.origin)
    }
}
