package com.trian0.viary.ui.checkpoint

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trian0.viary.MainDispatcherRule
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

class CheckpointViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: CheckpointViewModel

    private val fakeViaryEntity = ViaryEntity(
        id = "viary-1",
        name = "Viagem Teste",
        origin = "São Paulo",
        departureTime = Date(),
        initialBudget = 1000.0,
        status = Viary.ViaryStatus.IN_PROGRESS,
        finalBudget = 0.0,
        kmEnd = 0f,
        selectedImage = null,
        climate = null,
        latitudeOrigin = 0.0,
        longitudeOrigin = 0.0,
        latitudeArrival = 0.0,
        longitudeArrival = 0.0,
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
    }

    private fun createViewModel() {
        viewModel = CheckpointViewModel(savedStateHandle, repository)
    }

    @Test
    fun `OnCheckpointNameChanged - deve atualizar o nome e limpar o erro de validacao`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial (ou pós-init)

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointNameChanged("Museu Nacional"))
            val state = awaitItem()
            assertEquals("Museu Nacional", state.checkpointName)
            assertFalse(state.checkpointNameError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnCheckpointBudgetChanged - deve atualizar budget e recalcular preview`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem() // consome qualquer estado pendente do init

            // Budget "10050" → dígitos → 10050 / 100.0 = 100.50
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged("10050"))
            val state = awaitItem()
            assertEquals("10050", state.checkpointBudget)
            assertEquals(100.50, state.currentExpense, 0.01)
            assertEquals(100.50, state.previewAccumulated, 0.01)  // 0 acumulado + 100.50
            assertEquals(899.50, state.previewRemaining, 0.01)     // 1000 inicial - 100.50
            assertFalse(state.checkpointBudgetError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveCheckpointClicked - quando campos validos, deve mostrar dialog de sucesso`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem() // aguarda init do ViewModel

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointNameChanged("Parada 1"))
            awaitItem()
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged("5000"))
            awaitItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked)
            val state = expectMostRecentItem()
            assertTrue(state.showSuccessDialog)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveCheckpointClicked - quando nome vazio, deve mostrar erro de validacao`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged("5000"))
            awaitItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked)
            val state = awaitItem()
            assertTrue(state.checkpointNameError)
            assertFalse(state.showSuccessDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveCheckpointClicked - quando budget vazio, deve mostrar erro de validacao`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointNameChanged("Parada 1"))
            awaitItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked)
            val state = awaitItem()
            assertTrue(state.checkpointBudgetError)
            assertFalse(state.showSuccessDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnImageAdded - deve adicionar URI a lista capturedImages`() = runTest {
        val uri = mockk<Uri>()
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnImageAdded(uri))
            val state = awaitItem()
            assertTrue(state.capturedImages.contains(uri))
            assertEquals(1, state.capturedImages.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnImageRemoved - deve remover URI da lista capturedImages`() = runTest {
        val uri = mockk<Uri>()
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnImageAdded(uri))
            awaitItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnImageRemoved(uri))
            val state = awaitItem()
            assertFalse(state.capturedImages.contains(uri))
            assertTrue(state.capturedImages.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDismissErrorDialog - deve fechar o dialog de erro apos falha no repository`() = runTest {
        coEvery { repository.saveCheckpointWithPath(any(), any(), any()) } throws RuntimeException("erro ao salvar")
        createViewModel()

        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointNameChanged("Parada 1"))
            awaitItem()
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged("5000"))
            awaitItem()

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked)
            val errorState = expectMostRecentItem()
            assertTrue(errorState.showErrorDialog)

            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnDismissErrorDialog)
            val dismissedState = awaitItem()
            assertFalse(dismissedState.showErrorDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
