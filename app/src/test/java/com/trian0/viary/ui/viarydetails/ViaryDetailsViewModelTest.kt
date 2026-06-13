package com.trian0.viary.ui.viarydetails

import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.MainDispatcherRule
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.models.ViaryWithCheckpoints
import com.trian0.viary.data.repositories.ViaryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ViaryDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private lateinit var viewModel: ViaryDetailsViewModel

    private val baseViaryEntity = ViaryEntity(
        id = "viary-1",
        name = "Viagem ao Rio",
        origin = "São Paulo",
        departureTime = Date(0),
        initialBudget = 2000.0,
        status = Viary.ViaryStatus.COMPLETED,
        finalBudget = 1800.0,
        kmEnd = 450f,
        selectedImage = null,
        climate = "SUNNY",
        latitudeOrigin = -23.5,
        longitudeOrigin = -46.6,
        latitudeArrival = -22.9,
        longitudeArrival = -43.1,
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        viewModel = ViaryDetailsViewModel(repository)
    }

    @Test
    fun `Load - quando viary existe, deve popular estado com viary e checkpoints`() = runTest {
        val checkpointEntity = CheckpointEntity(viaryId = "viary-1", placeName = "Parada Carioca")
        val viaryWithCheckpoints = ViaryWithCheckpoints(baseViaryEntity, listOf(checkpointEntity))
        coEvery { repository.getViaryWithCheckpointsById("viary-1") } returns viaryWithCheckpoints

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("viary-1"))
            val state = awaitItem()

            assertNotNull(state.viary)
            assertEquals("Viagem ao Rio", state.viary?.name)
            assertEquals(1, state.checkpoints.size)
            assertEquals("Parada Carioca", state.checkpoints[0].placeName)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load - quando viary nao encontrado, deve apenas desligar o loading`() = runTest {
        coEvery { repository.getViaryWithCheckpointsById(any()) } returns null

        viewModel.uiState.test {
            awaitItem() // estado inicial

            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("inexistente"))
            val state = awaitItem()

            assertNull(state.viary)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load - deve formatar duracao corretamente com horas e minutos`() = runTest {
        // departureTime = 0ms, checkpoint.time = 2h30m = 9_000_000ms
        val departureMs = 0L
        val checkpointMs = 9_000_000L  // 2h 30m
        val entity = baseViaryEntity.copy(departureTime = Date(departureMs))
        val checkpointEntity = CheckpointEntity(viaryId = "viary-1", time = Date(checkpointMs))
        coEvery { repository.getViaryWithCheckpointsById("viary-1") } returns
            ViaryWithCheckpoints(entity, listOf(checkpointEntity))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("viary-1"))
            val state = awaitItem()
            assertEquals("2h 30m", state.durationFormatted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load - quando departureTime e nulo, duracao deve ser placeholder`() = runTest {
        val entity = baseViaryEntity.copy(departureTime = null)
        val checkpointEntity = CheckpointEntity(viaryId = "viary-1")
        coEvery { repository.getViaryWithCheckpointsById("viary-1") } returns
            ViaryWithCheckpoints(entity, listOf(checkpointEntity))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("viary-1"))
            val state = awaitItem()
            assertEquals("--", state.durationFormatted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load - deve coletar todas as fotos dos checkpoints`() = runTest {
        val checkpoint = CheckpointEntity(
            viaryId = "viary-1",
            imageUri = "cover.jpg",
            images = "photo1.jpg,photo2.jpg",
        )
        coEvery { repository.getViaryWithCheckpointsById("viary-1") } returns
            ViaryWithCheckpoints(baseViaryEntity, listOf(checkpoint))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("viary-1"))
            val state = awaitItem()
            // cover vai primeiro, depois as imagens extras
            assertEquals(3, state.allPhotos.size)
            assertTrue(state.allPhotos.contains("cover.jpg"))
            assertTrue(state.allPhotos.contains("photo1.jpg"))
            assertTrue(state.allPhotos.contains("photo2.jpg"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load - quando repository lanca excecao, deve desligar o loading`() = runTest {
        coEvery { repository.getViaryWithCheckpointsById(any()) } throws RuntimeException("erro de banco")

        viewModel.uiState.test {
            awaitItem()
            viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load("viary-1"))
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
