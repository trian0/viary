package com.trian0.viary.ui.home

import com.trian0.viary.data.models.Viary
import com.trian0.viary.mvi.MviInterfaces

interface HomeContract {

    data class HomeUiState(
        val viaryInProgress: Viary? = null,
        val totalViary: Int = 0
    ) : MviInterfaces.UiState

    sealed class HomeIntent : MviInterfaces.UiIntent {}

    sealed class HomeEffect : MviInterfaces.UiEffect {}
}