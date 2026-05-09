package com.trian0.viary.ui.home

import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.mvi.MviInterfaces
import java.util.Date

interface HomeContract {

    data class HomeUiState(
        val viaryInProgress: Viary? = null,
        val totalViary: Int = 0,
        val isLoading: Boolean = true,
        val timeElapsed: Date? = null,
        val distanceTraveled: Float? = 0f,
        val greaterDistance: Float? = 0f,
        val showFinishErrorDialog: Boolean = false,
        val showInitErrorDialog: Boolean = false,
        val checkpoints: List<Checkpoint> = emptyList(),
    ) : MviInterfaces.UiState

    sealed class HomeIntent : MviInterfaces.UiIntent {
        object OnFinishViary : HomeIntent()
        object OnDismissFinishErrorDialog : HomeIntent()
        object OnDismissInitErrorDialog : HomeIntent()
    }

    sealed class HomeEffect : MviInterfaces.UiEffect {}
}