package com.trian0.viary.ui.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ViaryRepository,
    private val locationHelper: LocationHelper,
) : BaseViewModel<HomeContract.HomeIntent, HomeContract.HomeUiState, HomeContract.HomeEffect>() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    override fun createInitialState(): HomeContract.HomeUiState = HomeContract.HomeUiState()

    override fun handleIntent(intent: HomeContract.HomeIntent) {
        Log.d(TAG, "handleIntent: $intent")

        when (intent) {
            is HomeContract.HomeIntent.OnFinishViary -> {
                finishViary()
            }

            is HomeContract.HomeIntent.OnDismissFinishErrorDialog -> {
                setState { copy(showFinishErrorDialog = false) }
            }

            is HomeContract.HomeIntent.OnDismissInitErrorDialog -> {
                setState { copy(showInitErrorDialog = false, isLoading = true) }
                init()
            }


        }
    }

    fun init() {
        Log.d(TAG, "init: ")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val viary = repository.viaryInProgress.first()?.toViary()
                val totalViary = repository.getTotalViary()
                val greaterDistance = repository.getGreaterDistance()

                val checkpoints = if (viary != null) {
                    repository.getCheckpointsByViaryId(viary.id)
                } else emptyList()

                setState {
                    copy(
                        viaryInProgress = viary,
                        checkpoints = checkpoints,
                        totalViary = totalViary,
                        isLoading = false,
                        greaterDistance = greaterDistance,
                    )
                }

                repository.allCompleted.collect { entities ->
                    val completedViary = entities.map { it.toViary() }
                    val lastCheckpoints = completedViary.associate { viary ->
                        viary.id to repository.getCheckpointsByViaryId(viary.id).lastOrNull()
                    }

                    setState {
                        copy(
                            completedViary = completedViary,
                            lastCheckpoints = lastCheckpoints
                        )
                    }
                }

                if (viary != null) {
                    startTrackingDistance(
                        viary.latitudeOrigin,
                        viary.longitudeOrigin,
                        viary.kmEnd,
                        viary.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setState { copy(isLoading = false, showInitErrorDialog = true) }
            }
        }
    }

    private fun startTrackingDistance(
        startLat: Double,
        startLng: Double,
        savedDistance: Float,
        viaryId: String
    ) {
        Log.d(
            TAG,
            "startTrackingDistance: startLat: $startLat, startLng: $startLng, savedDistance: $savedDistance, viaryId: $viaryId"
        )
        viewModelScope.launch(Dispatchers.Default) {
            try {
                var lastLatitude = startLat
                var lastLongitude = startLng
                var totalDistance = savedDistance

                while (true) {
                    delay(1000L)

                    val currentLocation = locationHelper.getCurrentLocation()

                    if (currentLocation != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            lastLatitude, lastLongitude,
                            currentLocation.latitude, currentLocation.longitude,
                            results
                        )
                        totalDistance += results[0] / 1000f
                        lastLatitude = currentLocation.latitude
                        lastLongitude = currentLocation.longitude

                        repository.updateDistanceTraveled(viaryId, totalDistance)
                    }

                    setState { copy(distanceTraveled = totalDistance) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun finishViary() {
        Log.d(TAG, "finishViary:")
        showLoading()

        viewModelScope.launch {
            try {
                val state = currentState
                val currentLocation = locationHelper.getCurrentLocation()

                repository.finishViary(
                    viaryId = state.viaryInProgress?.id ?: "",
                    status = Viary.ViaryStatus.COMPLETED,
                    latitude = currentLocation?.latitude ?: 0.0,
                    longitude = currentLocation?.longitude ?: 0.0
                )

                setState {
                    copy(
                        isLoading = false,
                        viaryInProgress = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setState { copy(isLoading = false, showFinishErrorDialog = true) }
            }
        }
    }

    private fun showLoading() {
        Log.d(TAG, "showLoading: ")
        setState { copy(isLoading = true) }
    }
}