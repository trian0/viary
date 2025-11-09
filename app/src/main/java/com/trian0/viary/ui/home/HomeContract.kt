package com.trian0.viary.ui.home

import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.mvi.MviInterfaces

interface HomeContract {

    data class HomeUiState(
        val viary: Viary? = null,
    ) : MviInterfaces.UiState

    sealed class HomeIntent : MviInterfaces.UiIntent {}

    sealed class HomeEffect : MviInterfaces.UiEffect {}
}