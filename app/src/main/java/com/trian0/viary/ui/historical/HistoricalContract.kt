package com.trian0.viary.ui.historical

import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.mvi.MviInterfaces

interface HistoricalContract {

    data class HistoricalUiState(
        val completedViary: List<Viary> = emptyList(),
        val lastCheckpoints: Map<String, Checkpoint?> = emptyMap(),
        val isLoading: Boolean = true,
    ) : MviInterfaces.UiState

    sealed class HistoricalIntent : MviInterfaces.UiIntent

    sealed class HistoricalEffect : MviInterfaces.UiEffect
}
