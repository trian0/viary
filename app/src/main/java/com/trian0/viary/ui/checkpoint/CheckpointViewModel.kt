package com.trian0.viary.ui.checkpoint

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.mvi.BaseViewModel
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CheckpointViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ViaryRepository,
) : BaseViewModel<CheckpointContract.CheckpointIntent, CheckpointContract.CheckpointUiState, CheckpointContract.CheckpointEffect>() {

    companion object {
        private const val TAG = "CheckpointViewModel"
        private const val KEY_CHECKPOINT_NAME = "checkpointName"
        private const val KEY_CHECKPOINT_BUDGET = "checkpointBudget"
        private const val KEY_COVER_PATH = "coverImagePath"
    }

    private var imageCopyJob: Job? = null
    private var currentCoverFilePath: String? = null
    private var savedSuccessfully = false

    init {
        val restoredName: String = savedStateHandle[KEY_CHECKPOINT_NAME] ?: ""
        val restoredBudget: String = savedStateHandle[KEY_CHECKPOINT_BUDGET] ?: ""
        val restoredCoverPath: String? = savedStateHandle[KEY_COVER_PATH]

        currentCoverFilePath = restoredCoverPath

        if (restoredName.isNotEmpty() || restoredBudget.isNotEmpty() || restoredCoverPath != null) {
            setState {
                copy(
                    checkpointName = restoredName,
                    checkpointBudget = restoredBudget,
                    checkpointCoverPath = restoredCoverPath,
                )
            }
        }

        viewModelScope.launch {
            try {
                val viary = repository.viaryInProgress.first()?.toViary()
                val accumulated = repository.getCheckpointsByViaryId(viary?.id ?: "")
                    .sumOf { it.expense }
                val remaining = (viary?.initialBudget ?: 0.0) - accumulated

                setState {
                    copy(
                        viaryName = viary?.name ?: "",
                        viaryId = viary?.id ?: "",
                        initialBudget = viary?.initialBudget ?: 0.0,
                        accumulatedExpense = accumulated,
                        remainingBudget = remaining,
                        previewAccumulated = accumulated,
                        previewRemaining = remaining
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun createInitialState() = CheckpointContract.CheckpointUiState()

    override fun handleIntent(intent: CheckpointContract.CheckpointIntent) {
        Log.d(TAG, "handleIntent: $intent")

        when (intent) {
            is CheckpointContract.CheckpointIntent.OnCoverImageSelected -> {
                intent.uri?.let { uri ->
                    imageCopyJob?.cancel()
                    imageCopyJob = viewModelScope.launch {
                        val path = repository.copyImageToStorage(uri)
                        if (path != null) {
                            currentCoverFilePath = path
                            savedStateHandle[KEY_COVER_PATH] = path
                            setState { copy(checkpointCoverPath = path) }
                        } else {
                            setEffect { CheckpointContract.CheckpointEffect.ShowError("Failed to save cover image") }
                        }
                    }
                }
            }

            is CheckpointContract.CheckpointIntent.OnCheckpointNameChanged -> {
                savedStateHandle[KEY_CHECKPOINT_NAME] = intent.name
                setState {
                    copy(checkpointName = intent.name, checkpointNameError = false)
                }
            }

            is CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged -> {
                savedStateHandle[KEY_CHECKPOINT_BUDGET] = intent.budget
                val expense = intent.budget
                    .filter { it.isDigit() }
                    .toLongOrNull() ?: 0L
                val expenseValue = expense / 100.0

                val newAccumulated = currentState.accumulatedExpense + expenseValue
                val newRemaining = currentState.initialBudget - newAccumulated

                setState {
                    copy(
                        checkpointBudget = intent.budget,
                        checkpointBudgetError = false,
                        currentExpense = expenseValue,
                        previewAccumulated = newAccumulated,
                        previewRemaining = newRemaining
                    )
                }
            }

            is CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked -> {
                saveCheckpoint()
            }

            is CheckpointContract.CheckpointIntent.OnImageAdded -> {
                setState {
                    copy(capturedImages = capturedImages + intent.uri)
                }
            }

            is CheckpointContract.CheckpointIntent.OnImageRemoved -> {
                setState {
                    copy(capturedImages = capturedImages - intent.uri)
                }
            }

            is CheckpointContract.CheckpointIntent.OnDismissErrorDialog -> {
                setState {
                    copy(showErrorDialog = false)
                }
            }
        }
    }

    private fun saveCheckpoint() {
        viewModelScope.launch {
            try {
                if (!validateFields()) {
                    return@launch
                }

                setState { copy(isLoading = true) }

                val state = currentState

                val expense = state.checkpointBudget
                    .filter { it.isDigit() }
                    .toLongOrNull() ?: 0L
                val expenseValue = expense / 100.0

                val newAccumulated = state.accumulatedExpense + expenseValue
                val newRemaining = state.initialBudget - newAccumulated

                val checkpoint = Checkpoint(
                    viaryId = state.viaryId,
                    placeName = state.checkpointName,
                    expense = expenseValue,
                )

                repository.saveCheckpointWithPath(checkpoint, state.checkpointCoverPath, state.capturedImages)

                savedSuccessfully = true
                savedStateHandle.remove<String>(KEY_CHECKPOINT_NAME)
                savedStateHandle.remove<String>(KEY_CHECKPOINT_BUDGET)
                savedStateHandle.remove<String>(KEY_COVER_PATH)

                setState {
                    copy(
                        accumulatedExpense = newAccumulated,
                        remainingBudget = newRemaining,
                        isLoading = false,
                        showSuccessDialog = true,
                    )
                }

                setEffect { CheckpointContract.CheckpointEffect.CheckpointSavedSuccessfully }

                Log.d(TAG, "Checkpoint criado com sucesso: $checkpoint")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Erro ao criar checkpoint", e)
                setState { copy(isLoading = false, showErrorDialog = true) }
                setEffect {
                    CheckpointContract.CheckpointEffect.ShowError(
                        e.message ?: "Erro ao criar checkpoint"
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

        if (state.checkpointName.isBlank()) {
            setState { copy(checkpointNameError = true) }
            isValid = false
        }

        if (state.checkpointBudget.isBlank()) {
            setState { copy(checkpointBudgetError = true) }
            isValid = false
        }

        return isValid
    }
}
