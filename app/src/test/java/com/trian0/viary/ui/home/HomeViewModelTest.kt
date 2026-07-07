package com.trian0.viary.ui.home

import android.location.Location
import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.MainDispatcherRule
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.LocationHelper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
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

    private var distanceBetweenMetersStub: (Location, Location) -> Float = { _, _ -> 0f }

    @Before
    fun setup() {
        mockkStatic(Log::class)
        mockkStatic(FirebaseCrashlytics::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { FirebaseCrashlytics.getInstance() } returns mockk(relaxed = true)
        every { repository.viaryInProgress } returns flowOf(null)
        every { repository.allCompleted } returns flowOf(emptyList())
        every { locationHelper.locationUpdates() } returns flowOf()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(
            repository,
            locationHelper,
            mainDispatcherRule.testDispatcher,
            distanceBetweenMeters = { from, to -> distanceBetweenMetersStub(from, to) },
        )
    }

    private fun fakeLocation(hasAccuracyValue: Boolean = true, accuracyValue: Float = 5f): Location {
        val location = mockk<Location>(relaxed = true)
        every { location.hasAccuracy() } returns hasAccuracyValue
        every { location.accuracy } returns accuracyValue
        return location
    }

    private fun stubDistanceBetween(meters: Float) {
        distanceBetweenMetersStub = { _, _ -> meters }
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
    fun `init - allCompleted e um Flow que nunca completa, mas tracking deve iniciar mesmo assim`() = runTest {
        // dao.getAllCompleted() no Room retorna um Flow que fica vivo pra sempre (nunca completa),
        // diferente de flowOf() usado nos outros testes. Regressao para bug em que o collect desse
        // Flow, se ficasse antes do startTrackingDistance no mesmo bloco, travava o init() pra sempre.
        val neverCompletingFlow = MutableSharedFlow<List<ViaryEntity>>()
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        every { repository.allCompleted } returns neverCompletingFlow
        coEvery { repository.getCheckpointsByViaryId("viary-1") } returns emptyList()
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init

            assertEquals("Viagem Teste", state.viaryInProgress?.name)
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
    fun `OnFinishViary - quando sucesso, deve limpar viaryInProgress e recalcular total e maior distancia`() = runTest {
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        coEvery { repository.getTotalViary() } returns 1
        coEvery { repository.getGreaterDistance() } returns 0f
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            awaitItem() // estado pós-init

            coEvery { repository.getTotalViary() } returns 2
            coEvery { repository.getGreaterDistance() } returns 42f

            viewModel.onIntent(HomeContract.HomeIntent.OnFinishViary)
            val state = expectMostRecentItem() // estado final após conclusão
            assertNull(state.viaryInProgress)
            assertFalse(state.isLoading)
            assertEquals(2, state.totalViary)
            assertEquals(42f, state.greaterDistance)
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

    @Test
    fun `nextTrackingState - primeira leitura de GPS apenas fixa a referencia, sem somar distancia`() = runTest {
        stubDistanceBetween(meters = 1000f) // se fosse contabilizado, ficaria óbvio (1km)
        createViewModel()

        val initial = HomeViewModel.TrackingState(lastLocation = null, totalDistanceKm = 0f)
        val next = viewModel.nextTrackingState(initial, fakeLocation())

        assertEquals(0f, next.totalDistanceKm)
    }

    @Test
    fun `nextTrackingState - segunda leitura com deslocamento real soma a distancia`() = runTest {
        createViewModel()

        val reference = fakeLocation()
        val afterFirstTick = viewModel.nextTrackingState(
            HomeViewModel.TrackingState(lastLocation = null, totalDistanceKm = 0f),
            reference,
        )

        stubDistanceBetween(meters = 1000f) // 1km de deslocamento real
        val afterSecondTick = viewModel.nextTrackingState(afterFirstTick, fakeLocation())

        assertEquals(1f, afterSecondTick.totalDistanceKm)
    }

    @Test
    fun `nextTrackingState - leitura com baixa acuracia e ignorada`() = runTest {
        createViewModel()

        val afterFirstTick = viewModel.nextTrackingState(
            HomeViewModel.TrackingState(lastLocation = null, totalDistanceKm = 0f),
            fakeLocation(),
        )

        stubDistanceBetween(meters = 1000f)
        val afterSecondTick = viewModel.nextTrackingState(
            afterFirstTick,
            fakeLocation(accuracyValue = 999f),
        )

        assertEquals(0f, afterSecondTick.totalDistanceKm)
        assertEquals(afterFirstTick.lastLocation, afterSecondTick.lastLocation)
    }

    @Test
    fun `nextTrackingState - deslocamento abaixo do limiar minimo e ignorado`() = runTest {
        createViewModel()

        val reference = fakeLocation()
        val afterFirstTick = viewModel.nextTrackingState(
            HomeViewModel.TrackingState(lastLocation = null, totalDistanceKm = 0f),
            reference,
        )

        stubDistanceBetween(meters = 3f) // abaixo de MIN_DISTANCE_METERS (5f), é ruído de GPS
        val afterSecondTick = viewModel.nextTrackingState(afterFirstTick, fakeLocation())

        assertEquals(0f, afterSecondTick.totalDistanceKm)
        assertEquals(reference, afterSecondTick.lastLocation)
    }

    @Test
    fun `startTrackingDistance - processa atualizacoes continuas de localizacao`() = runTest {
        val locationsFlow = MutableSharedFlow<Location>(extraBufferCapacity = 1)
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        coEvery { repository.getCheckpointsByViaryId(any()) } returns emptyList()
        every { locationHelper.locationUpdates() } returns locationsFlow
        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            awaitItem() // estado pós-init (viary carregado)

            locationsFlow.emit(fakeLocation()) // primeira leitura só fixa a referência

            stubDistanceBetween(meters = 1000f)
            locationsFlow.emit(fakeLocation()) // segunda leitura soma 1km

            val state = awaitItem()
            assertEquals(1f, state.distanceTraveled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `startTrackingDistance - erro ao processar uma atualizacao nao interrompe as proximas`() = runTest {
        val locationsFlow = MutableSharedFlow<Location>(extraBufferCapacity = 1)
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        coEvery { repository.getCheckpointsByViaryId(any()) } returns emptyList()
        every { locationHelper.locationUpdates() } returns locationsFlow

        var updateCalls = 0
        coEvery { repository.updateDistanceTraveled(any(), any()) } answers {
            updateCalls++
            if (updateCalls == 1) throw RuntimeException("falha transitória")
        }

        createViewModel()

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            awaitItem() // estado pós-init

            locationsFlow.emit(fakeLocation()) // fixa a referência

            stubDistanceBetween(meters = 1000f)
            locationsFlow.emit(fakeLocation()) // deveria somar 1km, mas o repository lança na 1ª chamada
            advanceUntilIdle()

            // a exceção foi contida: nenhum novo estado chegou a ser publicado por essa leitura
            locationsFlow.emit(fakeLocation()) // próxima leitura deve ser processada normalmente

            val state = awaitItem()
            assertEquals(1f, state.distanceTraveled)
            assertEquals(2, updateCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
