package com.trian0.viary.helpers

import android.content.Context
import android.location.Location
import android.os.SystemClock
import androidx.core.content.edit

class TrackingStateStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(viaryId: String, location: Location) {
        prefs.edit {
            putString(KEY_VIARY_ID, viaryId)
                .putLong(KEY_LATITUDE, location.latitude.toRawBits())
                .putLong(KEY_LONGITUDE, location.longitude.toRawBits())
                .putFloat(KEY_ACCURACY, location.accuracy)
                .putLong(KEY_SAVED_AT, System.currentTimeMillis())
        }
    }

    fun load(viaryId: String): Location? {
        if (prefs.getString(KEY_VIARY_ID, null) != viaryId) return null

        val savedAt = prefs.getLong(KEY_SAVED_AT, 0L)
        if (savedAt == 0L) return null

        val ageMillis = (System.currentTimeMillis() - savedAt).coerceAtLeast(0L)

        return Location(PROVIDER).apply {
            latitude = Double.fromBits(prefs.getLong(KEY_LATITUDE, 0L))
            longitude = Double.fromBits(prefs.getLong(KEY_LONGITUDE, 0L))
            accuracy = prefs.getFloat(KEY_ACCURACY, 0f)
            time = savedAt
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos() - ageMillis * 1_000_000L
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        const val PREFS_NAME = "viary_tracking"
        const val PROVIDER = "persisted"
        const val KEY_VIARY_ID = "viary_id"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ACCURACY = "accuracy"
        const val KEY_SAVED_AT = "saved_at"
    }
}
