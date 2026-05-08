package com.trian0.viary.ui.checkpoint

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.data.utils.saveImageToInternalStorage
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CheckpointViewModel(
    private val repository: ViaryRepository,
) : BaseViewModel<CheckpointContract.CheckpointIntent, CheckpointContract.CheckpointUiState, CheckpointContract.CheckpointEffect>() {

    companion object {
        private const val TAG = "CheckpointViewModel"
    }

    init {
        viewModelScope.launch {
            try {
                val viary = repository.viaryInProgress.first()?.toViary()
                val checkpoints = repository.getCheckpointsByViaryId(viary?.id ?: "")
                val accumulated = checkpoints.sumOf { it.expense }
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

    override fun createInitialState(): CheckpointContract.CheckpointUiState =
        CheckpointContract.CheckpointUiState()

    override fun handleIntent(intent: CheckpointContract.CheckpointIntent) {
        Log.d(TAG, "handleIntent: $intent")

        when (intent) {
            is CheckpointContract.CheckpointIntent.OnCoverImageSelected -> {
                setState {
                    copy(checkpointCoverUri = intent.uri)
                }
            }

            is CheckpointContract.CheckpointIntent.OnCheckpointNameChanged -> {
                setState {
                    copy(checkpointName = intent.name, checkpointNameError = false)
                }
            }

            is CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged -> {
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
        }
    }

    private fun saveCheckpoint() {
        viewModelScope.launch {
            try {
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

                repository.saveCheckpoint(checkpoint, state.checkpointCoverUri, state.capturedImages)

                setState {
                    copy(
                        accumulatedExpense = newAccumulated,
                        remainingBudget = newRemaining
                    )
                }

                setEffect { CheckpointContract.CheckpointEffect.CheckpointSavedSuccessfully }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}