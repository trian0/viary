package com.trian0.viary.helpers

import android.util.Log
import com.trian0.viary.data.models.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class DirectionsClient(
    private val httpClient: OkHttpClient,
    private val accessToken: String,
) {

    companion object {
        private const val TAG = "DirectionsClient"
        private const val BASE_URL = "https://api.mapbox.com/directions/v5/mapbox/driving"
        const val MAX_WAYPOINTS_PER_REQUEST = 25

        fun parseRoute(json: String): List<GeoPoint> {
            val coordinates = JSONObject(json)
                .getJSONArray("routes")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")
            return List(coordinates.length()) { i ->
                val pair = coordinates.getJSONArray(i)
                GeoPoint(latitude = pair.getDouble(1), longitude = pair.getDouble(0))
            }
        }

        fun windows(waypoints: List<GeoPoint>): List<List<GeoPoint>> {
            val step = MAX_WAYPOINTS_PER_REQUEST - 1
            return (0 until waypoints.size - 1 step step).map { start ->
                waypoints.subList(start, minOf(start + MAX_WAYPOINTS_PER_REQUEST, waypoints.size))
            }
        }
    }

    suspend fun route(waypoints: List<GeoPoint>): List<GeoPoint> {
        if (waypoints.size < 2 || accessToken.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                windows(waypoints).flatMapIndexed { index, window ->
                    val leg = fetchLeg(window)
                    if (index == 0) leg else leg.drop(1)
                }
            } catch (e: IOException) {
                Log.w(TAG, "rota indisponivel: ${e.message}")
                emptyList()
            } catch (e: JSONException) {
                Log.w(TAG, "resposta de rota invalida: ${e.message}")
                emptyList()
            }
        }
    }

    private fun fetchLeg(window: List<GeoPoint>): List<GeoPoint> {
        val coordinates = window.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "$BASE_URL/$coordinates?geometries=geojson&overview=full&access_token=$accessToken"
        httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return parseRoute(response.body?.string() ?: throw IOException("corpo vazio"))
        }
    }
}
