package com.trian0.viary.ui.create

import android.net.Uri
import com.trian0.viary.mvi.MviInterfaces

interface CreateContract {

    data class CreateUiState(
        val viaryName: String = "",
        val departureLocation: String = "",
        val currentKm: String = "",
        val coverImageUri: Uri? = null,
        val isLoading: Boolean = false,
        val viaryNameError: Boolean = false,
        val departureLocationError: Boolean = false,
        val currentKmError: Boolean = false,
        val showSuccessDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
        val climate: String = "",
    ) : MviInterfaces.UiState

    sealed class CreateIntent : MviInterfaces.UiIntent {
        data class OnViaryNameChanged(val name: String) : CreateIntent()
        data class OnDepartureLocationChanged(val location: String) : CreateIntent()
        data class OnCurrentKmChanged(val km: String) : CreateIntent()
        data class OnCoverImageSelected(val uri: Uri?) : CreateIntent()
        data class OnClimateChanged(val climate: String) : CreateIntent()
        object OnStartTripClicked : CreateIntent()
        object OnDismissErrorDialog : CreateIntent()
    }

    sealed class CreateEffect : MviInterfaces.UiEffect {
        object TripCreatedSuccessfully : CreateEffect()
        data class ShowError(val message: String) : CreateEffect()
    }
}