package com.trian0.viary.ui.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ViaryRepository,
    private val locationHelper: LocationHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val distanceBetweenMeters: (Location, Location) -> Float = { from, to ->
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
        results[0]
    },
) : BaseViewModel<HomeContract.HomeIntent, HomeContract.HomeUiState, HomeContract.HomeEffect>() {

    companion object {
        private const val TAG = "HomeViewModel"
        internal const val MIN_ACCURACY_METERS = 30f
        internal const val MIN_DISTANCE_METERS = 5f
    }

    internal data class TrackingState(val lastLocation: Location?, val totalDistanceKm: Float)

    private var trackingJob: Job? = null

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

        viewModelScope.launch(ioDispatcher) {
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

                if (viary != null) {
                    startTrackingDistance(viary.kmEnd, viary.id)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                setState { copy(isLoading = false, showInitErrorDialog = true) }
            }
        }

        viewModelScope.launch(ioDispatcher) {
            repository.allCompleted.collect { entities ->
                try {
                    val last = entities.firstOrNull()?.toViary()
                    val lastCheckpoint = if (last != null)
                        repository.getCheckpointsByViaryId(last.id).lastOrNull()
                    else null

                    setState {
                        copy(
                            lastCompletedViary = last,
                            lastCompletedCheckpoint = lastCheckpoint,
                        )
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startTrackingDistance(savedDistance: Float, viaryId: String) {
        Log.d(TAG, "startTrackingDistance: savedDistance: $savedDistance, viaryId: $viaryId")

        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(ioDispatcher) {
            var state = TrackingState(lastLocation = null, totalDistanceKm = savedDistance)

            locationHelper.locationUpdates().collect { location ->
                try {
                    Log.d(
                        TAG,
                        "locationUpdate: lat=${location.latitude}, lng=${location.longitude}, " +
                            "accuracy=${location.accuracy}, hasAccuracy=${location.hasAccuracy()}"
                    )

                    val nextState = nextTrackingState(state, location)

                    if (nextState.totalDistanceKm != state.totalDistanceKm) {
                        Log.d(
                            TAG,
                            "distancia atualizada: ${state.totalDistanceKm}km -> ${nextState.totalDistanceKm}km " +
                                "(viaryId=$viaryId)"
                        )
                        repository.updateDistanceTraveled(viaryId, nextState.totalDistanceKm)
                    }

                    state = nextState
                    setState { copy(distanceTraveled = state.totalDistanceKm) }
                } catch (e: Exception) {
                    Log.e(TAG, "erro ao processar atualizacao de localizacao", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                }
            }
        }
    }

    // função pura de propósito, para poder ser testada sem depender do loop/coroutine
    internal fun nextTrackingState(current: TrackingState, newLocation: Location): TrackingState {
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

        return if (distanceMeters >= MIN_DISTANCE_METERS) {
            Log.d(TAG, "deslocamento aceito: ${distanceMeters}m desde a ultima referencia")
            TrackingState(newLocation, current.totalDistanceKm + distanceMeters / 1000f)
        } else {
            Log.d(TAG, "deslocamento abaixo do limiar (${distanceMeters}m < ${MIN_DISTANCE_METERS}m), ignorado")
            current
        }
    }

    private fun isAccurateEnough(location: Location) =
        !location.hasAccuracy() || location.accuracy <= MIN_ACCURACY_METERS

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
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
                FirebaseCrashlytics.getInstance().recordException(e)
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