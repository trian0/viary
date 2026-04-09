package com.trian0.viary.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class LocationHelper(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val location = suspendCancellableCoroutine { continuation ->
            val cancelTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation { cancelTokenSource.cancel() }

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancelTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }
        }

        if (location != null) return location

        val lastLocation = suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }

        val ageMs = lastLocation?.let { System.currentTimeMillis() - it.time }
        val isRecent = ageMs != null && ageMs < TimeUnit.MINUTES.toMillis(5)

        return if (isRecent) lastLocation else null
    }
}