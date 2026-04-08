package com.trian0.viary.ui.checkpoint

import android.net.Uri
import com.trian0.viary.mvi.MviInterfaces

interface CheckpointContract {

    data class CheckpointUiState(
        val viaryName: String = "",
        val checkpointCoverUri: Uri? = null,
        val checkpointName: String = "",
        val checkpointNameError: Boolean = false,
    ) : MviInterfaces.UiState

    sealed class CheckpointIntent : MviInterfaces.UiIntent {
        data class OnCoverImageSelected(val uri: Uri?) : CheckpointIntent()
        data class OnCheckpointNameChanged(val name: String) : CheckpointIntent()
    }

    sealed class CheckpointEffect : MviInterfaces.UiEffect {}

}