package com.trian0.viary.ui.create

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.mvi.BaseViewModel
import java.io.File
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ViaryRepository,
    private val locationHelper: LocationHelper,
) : BaseViewModel<CreateContract.CreateIntent, CreateContract.CreateUiState, CreateContract.CreateEffect>() {

    companion object {
        private const val TAG = "CreateViewModel"
        private const val KEY_VIARY_NAME = "viaryName"
        private const val KEY_DEPARTURE = "departureLocation"
        private const val KEY_BUDGET = "currentBudget"
        private const val KEY_COVER_PATH = "coverImagePath"
    }

    private var imageCopyJob: Job? = null
    private var currentCoverFilePath: String? = null
    private var savedSuccessfully = false

    override fun createInitialState() = CreateContract.CreateUiState()

    init {
        val restoredName: String = savedStateHandle[KEY_VIARY_NAME] ?: ""
        val restoredDeparture: String = savedStateHandle[KEY_DEPARTURE] ?: ""
        val restoredBudget: String = savedStateHandle[KEY_BUDGET] ?: ""
        val restoredCoverPath: String? = savedStateHandle[KEY_COVER_PATH]

        currentCoverFilePath = restoredCoverPath

        if (restoredName.isNotEmpty() || restoredDeparture.isNotEmpty()
            || restoredBudget.isNotEmpty() || restoredCoverPath != null
        ) {
            setState {
                copy(
                    viaryName = restoredName,
                    departureLocation = restoredDeparture,
                    currentBudget = restoredBudget,
                    coverImagePath = restoredCoverPath,
                )
            }
        }
    }

    override fun handleIntent(intent: CreateContract.CreateIntent) {
        Log.d(TAG, "handleIntent: $intent")

        when (intent) {
            is CreateContract.CreateIntent.OnViaryNameChanged -> {
                savedStateHandle[KEY_VIARY_NAME] = intent.name
                setState {
                    copy(
                        viaryName = intent.name,
                        viaryNameError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnDepartureLocationChanged -> {
                savedStateHandle[KEY_DEPARTURE] = intent.location
                setState {
                    copy(
                        departureLocation = intent.location,
                        departureLocationError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnCurrentBudgetChanged -> {
                savedStateHandle[KEY_BUDGET] = intent.budget
                setState {
                    copy(
                        currentBudget = intent.budget,
                        currentBudgetError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnCoverImageSelected -> {
                intent.uri?.let { uri ->
                    imageCopyJob?.cancel()
                    imageCopyJob = viewModelScope.launch {
                        val path = repository.copyImageToStorage(uri)
                        if (path != null) {
                            currentCoverFilePath = path
                            savedStateHandle[KEY_COVER_PATH] = path
                            setState { copy(coverImagePath = path) }
                        } else {
                            setEffect { CreateContract.CreateEffect.ShowError("Failed to save cover image") }
                        }
                    }
                }
            }

            is CreateContract.CreateIntent.OnStartTripClicked -> {
                startTrip()
            }

            is CreateContract.CreateIntent.OnClimateChanged -> {
                setState {
                    copy(climate = intent.climate)
                }
            }

            is CreateContract.CreateIntent.OnDismissErrorDialog -> {
                setState { copy(showErrorDialog = false) }
            }
        }
    }

    private fun startTrip() {
        viewModelScope.launch {
            try {
                if (!validateFields()) {
                    return@launch
                }

                setState { copy(isLoading = true) }

                val state = currentState

                val currentBudget = (state.currentBudget.filter { it.isDigit() }.toLongOrNull() ?: 0L) / 100.0

                val location = locationHelper.getCurrentLocation()

                val viary = Viary(
                    name = state.viaryName,
                    origin = state.departureLocation,
                    departureTime = Date(),
                    initialBudget = currentBudget,
                    status = Viary.ViaryStatus.IN_PROGRESS,
                    kmEnd = 0f,
                    selectedImage = null,
                    climate = state.climate,
                    latitudeOrigin = location?.latitude ?: 0.0,
                    longitudeOrigin = location?.longitude ?: 0.0,
                )

                repository.createWithPath(viary, state.coverImagePath)

                savedSuccessfully = true
                savedStateHandle.remove<String>(KEY_VIARY_NAME)
                savedStateHandle.remove<String>(KEY_DEPARTURE)
                savedStateHandle.remove<String>(KEY_BUDGET)
                savedStateHandle.remove<String>(KEY_COVER_PATH)

                setState { copy(isLoading = false, showSuccessDialog = true) }
                setEffect { CreateContract.CreateEffect.TripCreatedSuccessfully }

                Log.d(TAG, "Viagem criada com sucesso: $viary")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e(TAG, "Erro ao criar viagem", e)
                setState { copy(isLoading = false, showErrorDialog = true) }
                setEffect {
                    CreateContract.CreateEffect.ShowError(
                        e.message ?: "Erro ao criar viagem"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!savedSuccessfully) {
            currentCoverFilePath?.let { File(it).delete() }
        }
    }

    private fun validateFields(): Boolean {
        val state = currentState
        var isValid = true

        if (state.viaryName.isBlank()) {
            setState { copy(viaryNameError = true) }
            isValid = false
        }

        if (state.departureLocation.isBlank()) {
            setState { copy(departureLocationError = true) }
            isValid = false
        }

        if (state.currentBudget.isBlank()) {
            setState { copy(currentBudgetError = true) }
            isValid = false
        } else {
            val digits = state.currentBudget.filter { it.isDigit() }
            val budget = digits.toLongOrNull()
            if (budget == null || budget <= 0) {
                setState { copy(currentBudgetError = true) }
                isValid = false
            }
        }

        return isValid
    }
}
