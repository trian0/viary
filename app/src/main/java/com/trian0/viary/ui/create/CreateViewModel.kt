package com.trian0.viary.ui.create

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.launch
import java.util.Date

class CreateViewModel(
    private val repository: ViaryRepository,
    private val locationHelper: LocationHelper,
) : BaseViewModel<CreateContract.CreateIntent, CreateContract.CreateUiState, CreateContract.CreateEffect>() {

    companion object {
        private const val TAG = "CreateViewModel"
    }

    override fun createInitialState(): CreateContract.CreateUiState = CreateContract.CreateUiState()

    override fun handleIntent(intent: CreateContract.CreateIntent) {
        Log.d(TAG, "handleIntent: $intent")

        when (intent) {
            is CreateContract.CreateIntent.OnViaryNameChanged -> {
                setState {
                    copy(
                        viaryName = intent.name,
                        viaryNameError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnDepartureLocationChanged -> {
                setState {
                    copy(
                        departureLocation = intent.location,
                        departureLocationError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnCurrentBudgetChanged -> {
                setState {
                    copy(
                        currentBudget = intent.budget,
                        currentBudgetError = false
                    )
                }
            }

            is CreateContract.CreateIntent.OnCoverImageSelected -> {
                setState {
                    copy(coverImageUri = intent.uri)
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

                repository.create(viary, state.coverImageUri)

                setState { copy(isLoading = false, showSuccessDialog = true) }
                setEffect { CreateContract.CreateEffect.TripCreatedSuccessfully }

                Log.d(TAG, "Viagem criada com sucesso: $viary")
            } catch (e: Exception) {
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