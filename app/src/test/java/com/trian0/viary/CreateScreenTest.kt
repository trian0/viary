package com.trian0.viary

import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.ui.create.CreateContract
import com.trian0.viary.ui.create.CreateViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private lateinit var viewModel: CreateViewModel

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        viewModel = CreateViewModel(repository)
    }

    @Test
    fun `quando o nome da viagem estiver vazio, deve retornar erro de validacao`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onIntent(CreateContract.CreateIntent.OnStartTripClicked)
            val stateWithError = awaitItem()

            assertEquals("Nome é obrigatório", stateWithError.viaryNameError)
            expectNoEvents()
        }
    }
}