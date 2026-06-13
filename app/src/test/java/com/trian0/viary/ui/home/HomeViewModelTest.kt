package com.trian0.viary.ui.home

import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.MainDispatcherRule
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.LocationHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private val locationHelper = mockk<LocationHelper>(relaxed = true)
    private lateinit var viewModel: HomeViewModel

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
        latitudeOrigin = -23.5,
        longitudeOrigin = -46.6,
        latitudeArrival = 0.0,
        longitudeArrival = 0.0,
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { repository.viaryInProgress } returns flowOf(null)
        every { repository.allCompleted } returns flowOf(emptyList())
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(repository, locationHelper)
    }

    @Test
    fun `init - sem viary em andamento, deve definir isLoading como false`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init
            assertNull(state.viaryInProgress)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init - com viary em andamento, deve carregar viary e checkpoints`() = runTest {
        val fakeCheckpoint = Checkpoint(viaryId = "viary-1", placeName = "Parada 1")
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        coEvery { repository.getCheckpointsByViaryId("viary-1") } returns listOf(fakeCheckpoint)
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init
            assertEquals("Viagem Teste", state.viaryInProgress?.name)
            assertEquals(1, state.checkpoints.size)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init - quando repository lanca excecao, deve mostrar dialog de erro`() = runTest {
        every { repository.viaryInProgress } throws RuntimeException("erro de banco")
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado de erro
            assertTrue(state.showInitErrorDialog)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnFinishViary - quando sucesso, deve limpar viaryInProgress`() = runTest {
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            awaitItem() // estado pós-init

            viewModel.onIntent(HomeContract.HomeIntent.OnFinishViary)
            val state = expectMostRecentItem() // estado final após conclusão
            assertNull(state.viaryInProgress)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnFinishViary - quando repository lanca excecao, deve mostrar dialog de erro`() = runTest {
        coEvery { repository.finishViary(any(), any(), any(), any()) } throws RuntimeException("falha ao finalizar")
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.onIntent(HomeContract.HomeIntent.OnFinishViary)
            val state = expectMostRecentItem() // estado após falha
            assertTrue(state.showFinishErrorDialog)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDismissFinishErrorDialog - deve fechar o dialog de erro de finalizacao`() = runTest {
        coEvery { repository.finishViary(any(), any(), any(), any()) } throws RuntimeException("falha")
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.onIntent(HomeContract.HomeIntent.OnFinishViary)
            val errorState = expectMostRecentItem()
            assertTrue(errorState.showFinishErrorDialog)

            viewModel.onIntent(HomeContract.HomeIntent.OnDismissFinishErrorDialog)
            val dismissedState = awaitItem()
            assertFalse(dismissedState.showFinishErrorDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDismissInitErrorDialog - deve fechar o dialog e recarregar os dados`() = runTest {
        every { repository.viaryInProgress } throws RuntimeException("erro")
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val errorState = awaitItem()
            assertTrue(errorState.showInitErrorDialog)

            every { repository.viaryInProgress } returns flowOf(null)

            viewModel.onIntent(HomeContract.HomeIntent.OnDismissInitErrorDialog)
            val restoredState = awaitItem() // setState dentro do handleIntent
            assertFalse(restoredState.showInitErrorDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
