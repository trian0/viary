package com.trian0.viary.ui.checkpoint

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
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

                setState {
                    copy(
                        viaryName = viary?.name ?: ""
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
        }
    }
}