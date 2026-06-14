package com.trian0.viary.ui.historical

import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.mvi.MviInterfaces

interface HistoricalContract {

    data class HistoricalUiState(
        val lastCheckpoints: Map<String, Checkpoint?> = emptyMap(),
    ) : MviInterfaces.UiState

    sealed class HistoricalIntent : MviInterfaces.UiIntent

    sealed class HistoricalEffect : MviInterfaces.UiEffect
}
