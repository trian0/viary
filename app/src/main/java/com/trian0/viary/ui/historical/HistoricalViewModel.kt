package com.trian0.viary.ui.historical

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoricalViewModel(
    private val repository: ViaryRepository,
) : BaseViewModel<HistoricalContract.HistoricalIntent, HistoricalContract.HistoricalUiState, HistoricalContract.HistoricalEffect>() {

    companion object {
        private const val TAG = "HistoricalViewModel"
    }

    val completedPaged = repository.completedPaged().cachedIn(viewModelScope)

    override fun createInitialState(): HistoricalContract.HistoricalUiState =
        HistoricalContract.HistoricalUiState()

    override fun handleIntent(intent: HistoricalContract.HistoricalIntent) {
        Log.d(TAG, "handleIntent: $intent")
    }

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.allCompleted.collect { entities ->
                    val completedViary = entities.map { it.toViary() }
                    val lastCheckpoints = completedViary.associate { viary ->
                        viary.id to repository.getCheckpointsByViaryId(viary.id).lastOrNull()
                    }
                    setState { copy(lastCheckpoints = lastCheckpoints) }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }
}
