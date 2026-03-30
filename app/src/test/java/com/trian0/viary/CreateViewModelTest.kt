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

class CreateViewModelTest {

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
    fun `quando todos os campos estao validos, deve criar a viagem com sucesso`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onIntent(CreateContract.CreateIntent.OnViaryNameChanged("Viagem de Férias"))
            assertEquals("Viagem de Férias", awaitItem().viaryName)

            viewModel.onIntent(CreateContract.CreateIntent.OnDepartureLocationChanged("São Paulo"))
            assertEquals("São Paulo", awaitItem().departureLocation)

            viewModel.onIntent(CreateContract.CreateIntent.OnCurrentKmChanged("100.5"))
            assertEquals("100.5", awaitItem().currentKm)

            viewModel.onIntent(CreateContract.CreateIntent.OnStartTripClicked)

            val finalState = expectMostRecentItem()
            assert(finalState.showSuccessDialog)
        }
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