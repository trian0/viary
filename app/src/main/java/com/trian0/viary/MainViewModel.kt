package com.trian0.viary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.repositories.ViaryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SplashNavState {
    data object Loading : SplashNavState()
    data object NavigateToHome : SplashNavState()
}

class MainViewModel(
    private val repository: ViaryRepository
) : ViewModel() {
    private val _navState = MutableStateFlow<SplashNavState>(SplashNavState.Loading)
    val navState: StateFlow<SplashNavState> = _navState.asStateFlow()
    var hasViaryInProgress by mutableStateOf(false)
        private set

    val keepSplashOn: StateFlow<Boolean> = _navState.map {
        it is SplashNavState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    init {
        viewModelScope.launch {
            checkInitialSetup()
        }

        viewModelScope.launch {
            repository.viaryInProgress.collect { viary ->
                hasViaryInProgress = viary != null
            }
        }
    }

    private suspend fun checkInitialSetup() {
        delay(50)
        _navState.value = SplashNavState.NavigateToHome
    }
}