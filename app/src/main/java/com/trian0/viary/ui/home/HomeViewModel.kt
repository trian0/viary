package com.trian0.viary.ui.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    }

    fun init() {
        Log.d(TAG, "init: ")

        viewModelScope.launch(Dispatchers.IO) {
            val viary = repository.getViaryInProgress()?.toViary()
            val totalViary = repository.getTotalViary()
            val greaterDistance = repository.getGreaterDistance()

            setState {
                copy(
                    viaryInProgress = viary,
                    totalViary = totalViary,
                    isLoading = false,
                    greaterDistance = greaterDistance,
                )
            }

            if (viary != null) {
                startTrackingDistance(
                    viary.latitude,
                    viary.longitude,
                    viary.kmEnd,
                    viary.id
                )
            }
        }
    }

    private fun startTrackingDistance(
        startLat: Double?,
        startLng: Double?,
        savedDistance: Float,
        viaryId: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            var lastLatitude = startLat ?: 0.0
            var lastLongitude = startLng ?: 0.0
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
        }
    }
}