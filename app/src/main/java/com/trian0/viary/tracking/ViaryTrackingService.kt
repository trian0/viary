package com.trian0.viary.tracking

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.trian0.viary.R
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.DistanceTracker
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.helpers.TrackingStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ViaryTrackingService : Service() {

    private val repository: ViaryRepository by inject()
    private val locationHelper: LocationHelper by inject()
    private val distanceTracker = DistanceTracker()
    private val stateStore by lazy { TrackingStateStore(applicationContext) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.app_name))
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(0f),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        if (trackingJob?.isActive != true) {
            trackingJob = scope.launch { followViaryInProgress() }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun followViaryInProgress() {
        repository.viaryInProgress
            .map { it?.id }
            .distinctUntilChanged()
            .collectLatest { viaryId ->
                if (viaryId == null) {
                    stateStore.clear()
                    stopSelf()
                } else {
                    track(viaryId)
                }
            }
    }

    private suspend fun track(viaryId: String) {
        val savedDistance = repository.viaryInProgress.first()?.kmEnd ?: 0f
        var state = DistanceTracker.State(
            lastLocation = stateStore.load(viaryId),
            totalDistanceKm = savedDistance,
        )
        updateNotification(savedDistance)

        locationHelper.locationUpdates().collect { location ->
            try {
                val next = distanceTracker.next(state, location)

                if (next.totalDistanceKm != state.totalDistanceKm) {
                    repository.updateDistanceTraveled(viaryId, next.totalDistanceKm)
                    updateNotification(next.totalDistanceKm)
                }
                next.lastLocation?.takeIf { it !== state.lastLocation }?.let { stateStore.save(viaryId, it) }
                state = next
            } catch (e: Exception) {
                Log.e(TAG, "erro ao processar atualizacao de localizacao", e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    private fun updateNotification(distanceKm: Float) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(distanceKm))
    }

    private fun buildNotification(distanceKm: Float): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(
                "${getString(R.string.home_screen_viary_in_progress_title)} · " +
                    getString(R.string.home_screen_distance, distanceKm)
            )
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "ViaryTrackingService"
        private const val CHANNEL_ID = "viary_tracking"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, ViaryTrackingService::class.java))
        }
    }
}
