package com.trian0.viary.helpers

import android.location.Location
import android.util.Log
import kotlin.math.max

class DistanceTracker(
    private val distanceBetweenMeters: (Location, Location) -> Float = { from, to ->
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
        results[0]
    },
) {

    companion object {
        private const val TAG = "DistanceTracker"
        const val MAX_ACCURACY_METERS = 30f
        const val MIN_DISTANCE_METERS = 5f

        const val MAX_SPEED_METERS_PER_SECOND = 100f

        const val MAX_CONSECUTIVE_JUMPS = 5
    }

    data class State(
        val lastLocation: Location?,
        val totalDistanceKm: Float,
        val rejectedJumps: Int = 0,
    )

    fun next(current: State, newLocation: Location): State {
        if (!isAccurateEnough(newLocation)) {
            Log.d(TAG, "leitura ignorada: acuracia insuficiente (${newLocation.accuracy}m)")
            return current
        }

        val previousLocation = current.lastLocation
            ?: run {
                Log.d(TAG, "primeira leitura: fixando referencia, sem somar distancia")
                return current.copy(lastLocation = newLocation)
            }

        val distanceMeters = distanceBetweenMeters(previousLocation, newLocation)

        if (isImpossibleJump(previousLocation, newLocation, distanceMeters)) {
            val rejected = current.rejectedJumps + 1
            if (rejected < MAX_CONSECUTIVE_JUMPS) {
                Log.d(
                    TAG,
                    "leitura ignorada: salto de ${distanceMeters}m incompativel com a velocidade"
                )
                return current.copy(rejectedJumps = rejected)
            }
            Log.d(
                TAG,
                "$rejected saltos seguidos: tratando como deslocamento real de ${distanceMeters}m"
            )
            return State(newLocation, current.totalDistanceKm + distanceMeters / 1000f)
        }

        val threshold = max(MIN_DISTANCE_METERS, accuracyOf(newLocation))
        if (distanceMeters < threshold) {
            Log.d(
                TAG,
                "deslocamento abaixo do limiar (${distanceMeters}m < ${threshold}m), ignorado"
            )
            return current.copy(rejectedJumps = 0)
        }

        Log.d(TAG, "deslocamento aceito: ${distanceMeters}m desde a ultima referencia")
        return State(newLocation, current.totalDistanceKm + distanceMeters / 1000f)
    }

    private fun isAccurateEnough(location: Location) =
        !location.hasAccuracy() || location.accuracy <= MAX_ACCURACY_METERS

    private fun accuracyOf(location: Location) =
        if (location.hasAccuracy()) location.accuracy else 0f

    private fun isImpossibleJump(from: Location, to: Location, distanceMeters: Float): Boolean {
        val elapsedSeconds = (to.elapsedRealtimeNanos - from.elapsedRealtimeNanos) / 1_000_000_000f
        if (elapsedSeconds <= 0f) return false
        return distanceMeters / elapsedSeconds > MAX_SPEED_METERS_PER_SECOND
    }
}
