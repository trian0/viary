package com.trian0.viary.helpers

import com.trian0.viary.data.models.GeoPoint
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionsClientTest {

    @Test
    fun `parseRoute - converte coordenadas lng,lat em GeoPoint`() {
        val json = """
            {"code":"Ok","routes":[{"geometry":{"type":"LineString",
            "coordinates":[[-46.6,-23.5],[-45.0,-23.0],[-43.1,-22.9]]}}]}
        """.trimIndent()

        assertEquals(
            listOf(GeoPoint(-23.5, -46.6), GeoPoint(-23.0, -45.0), GeoPoint(-22.9, -43.1)),
            DirectionsClient.parseRoute(json)
        )
    }

    @Test(expected = JSONException::class)
    fun `parseRoute - sem rotas na resposta lanca JSONException`() {
        DirectionsClient.parseRoute("""{"code":"NoRoute","routes":[]}""")
    }

    @Test
    fun `windows - ate 25 pontos gera uma requisicao`() {
        val points = List(25) { GeoPoint(it + 1.0, it + 1.0) }

        assertEquals(listOf(points), DirectionsClient.windows(points))
    }

    @Test
    fun `windows - acima de 25 pontos sobrepoe o ultimo ponto de cada janela`() {
        val points = List(30) { GeoPoint(it + 1.0, it + 1.0) }

        val windows = DirectionsClient.windows(points)

        assertEquals(2, windows.size)
        assertEquals(25, windows[0].size)
        assertEquals(6, windows[1].size)
        assertEquals(windows[0].last(), windows[1].first())
        assertEquals(points.last(), windows[1].last())
    }
}
