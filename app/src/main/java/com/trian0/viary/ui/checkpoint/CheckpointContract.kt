package com.trian0.viary.ui.checkpoint

import android.net.Uri
import com.trian0.viary.mvi.MviInterfaces

interface CheckpointContract {

    data class CheckpointUiState(
        val viaryName: String = "",
        val viaryId: String = "",
        val initialBudget: Double = 0.0,
        val accumulatedExpense: Double = 0.0,
        val remainingBudget: Double = 0.0,
        val checkpointCoverUri: Uri? = null,
        val checkpointName: String = "",
        val checkpointNameError: Boolean = false,
        val checkpointBudget: String = "",
        val checkpointBudgetError: Boolean = false,
        val currentExpense: Double = 0.0,
        val previewAccumulated: Double = 0.0,
        val previewRemaining: Double = 0.0,
        val capturedImages: List<Uri> = emptyList(),
        val isLoading: Boolean = false,
        val showSuccessDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
    ) : MviInterfaces.UiState

    sealed class CheckpointIntent : MviInterfaces.UiIntent {
        data class OnCoverImageSelected(val uri: Uri?) : CheckpointIntent()
        data class OnCheckpointNameChanged(val name: String) : CheckpointIntent()
        data class OnCheckpointBudgetChanged(val budget: String) : CheckpointIntent()
        object OnSaveCheckpointClicked : CheckpointIntent()
        data class OnImageAdded(val uri: Uri) : CheckpointIntent()
        data class OnImageRemoved(val uri: Uri) : CheckpointIntent()
        object OnDismissErrorDialog : CheckpointIntent()
    }

    sealed class CheckpointEffect : MviInterfaces.UiEffect {
        object CheckpointSavedSuccessfully : CheckpointEffect()
        data class ShowError(val message: String) : CheckpointEffect()
    }

}