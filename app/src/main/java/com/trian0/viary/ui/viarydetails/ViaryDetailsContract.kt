package com.trian0.viary.ui.viarydetails

import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.mvi.MviInterfaces

interface ViaryDetailsContract {

    data class ViaryDetailsUiState(
        val viary: Viary? = null,
        val checkpoints: List<Checkpoint> = emptyList(),
        val allPhotos: List<String> = emptyList(),
        val durationFormatted: String = "--",
        val isLoading: Boolean = true,
    ) : MviInterfaces.UiState

    sealed class ViaryDetailsIntent : MviInterfaces.UiIntent {
        data class Load(val viaryId: String) : ViaryDetailsIntent()
    }

    sealed class ViaryDetailsEffect : MviInterfaces.UiEffect
}
