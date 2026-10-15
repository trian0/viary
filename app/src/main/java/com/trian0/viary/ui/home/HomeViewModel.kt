package com.trian0.viary.ui.home

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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ViaryRepository,
    private val locationHelper: LocationHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseViewModel<HomeContract.HomeIntent, HomeContract.HomeUiState, HomeContract.HomeEffect>() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private var distanceJob: Job? = null

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

        observeDistance()

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
                        distanceTraveled = viary?.kmEnd ?: 0f,
                        checkpoints = checkpoints,
                        totalViary = totalViary,
                        isLoading = false,
                        greaterDistance = greaterDistance,
                    )
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

    private fun observeDistance() {
        distanceJob?.cancel()
        distanceJob = viewModelScope.launch(ioDispatcher) {
            try {
                repository.viaryInProgress.mapNotNull { it?.kmEnd }
                    .distinctUntilChanged()
                    .collect { km -> setState { copy(distanceTraveled = km) } }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        distanceJob?.cancel()
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

                val totalViary = repository.getTotalViary()
                val greaterDistance = repository.getGreaterDistance()

                setState {
                    copy(
                        isLoading = false,
                        viaryInProgress = null,
                        totalViary = totalViary,
                        greaterDistance = greaterDistance,
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