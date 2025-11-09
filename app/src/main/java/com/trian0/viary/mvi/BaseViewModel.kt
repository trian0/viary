package com.trian0.viary.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<Intent : MviInterfaces.UiIntent, State : MviInterfaces.UiState, Effect : MviInterfaces.UiEffect> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)

    val uiState = _uiState.asStateFlow()

    private val _intent: Channel<Intent> = Channel()

    private val _effect: Channel<Effect> = Channel()

    val effect = _effect.receiveAsFlow()

    val currentState: State
        get() = uiState.value

    init {
        subscribeToIntents()
    }

    private fun subscribeToIntents() {
        viewModelScope.launch {
            _intent.receiveAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }
    }

    fun onIntent(intent: Intent) {
        viewModelScope.launch { _intent.send(intent) }
    }

    protected fun setState(reduce: State.() -> State) {
        _uiState.value = uiState.value.reduce()
    }

    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    abstract fun createInitialState(): State

    abstract fun handleIntent(intent: Intent)
}