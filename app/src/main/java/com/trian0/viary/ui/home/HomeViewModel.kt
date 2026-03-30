package com.trian0.viary.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.data.repositories.toViary
import com.trian0.viary.mvi.BaseViewModel
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ViaryRepository
) : BaseViewModel<HomeContract.HomeIntent, HomeContract.HomeUiState, HomeContract.HomeEffect>() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    override fun createInitialState(): HomeContract.HomeUiState = HomeContract.HomeUiState()

    override fun handleIntent(intent: HomeContract.HomeIntent) {
        Log.d(TAG, "handleIntent: $intent")
    }

    fun init() {
        Log.d(TAG, "init: ")

        viewModelScope.launch {
            val viary = repository.getViaryInProgress()?.toViary()
            val totalViary = repository.getTotalViary()
            setState {
                copy(
                    viaryInProgress = viary,
                    totalViary = totalViary,
                )
            }
        }
    }

}