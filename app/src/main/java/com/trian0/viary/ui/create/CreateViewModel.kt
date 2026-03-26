package com.trian0.viary.ui.create

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.launch
import java.util.Date

class CreateViewModel(
    private val repository: ViaryRepository
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
                        viaryNameError = null
                    )
                }
            }

            is CreateContract.CreateIntent.OnDepartureLocationChanged -> {
                setState {
                    copy(
                        departureLocation = intent.location,
                        departureLocationError = null
                    )
                }
            }

            is CreateContract.CreateIntent.OnCurrentKmChanged -> {
                setState {
                    copy(
                        currentKm = intent.km,
                        currentKmError = null
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

                val kmStart = state.currentKm.toDoubleOrNull() ?: 0.0

                val viary = Viary(
                    name = state.viaryName,
                    origin = state.departureLocation,
                    departureTime = Date(),
                    kmStart = kmStart,
                    status = Viary.ViaryStatus.IN_PROGRESS,
                    kmEnd = 0.0,
                    selectedImage = null,
                )

                repository.create(viary, state.coverImageUri)

                setState { copy(isLoading = false, showSuccessDialog = true) }
                setEffect { CreateContract.CreateEffect.TripCreatedSuccessfully }

                Log.d(TAG, "Viagem criada com sucesso: $viary")

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar viagem", e)
                setState { copy(isLoading = false) }
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
            setState { copy(viaryNameError = "Nome é obrigatório") }
            isValid = false
        }

        if (state.departureLocation.isBlank()) {
            setState { copy(departureLocationError = "Localização é obrigatória") }
            isValid = false
        }

        if (state.currentKm.isBlank()) {
            setState { copy(currentKmError = "Kilometragem é obrigatória") }
            isValid = false
        } else {
            val km = state.currentKm.toDoubleOrNull()
            if (km == null || km < 0) {
                setState { copy(currentKmError = "Kilometragem inválida") }
                isValid = false
            }
        }

        return isValid
    }
}