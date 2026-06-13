package com.trian0.viary.ui.historical

import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.MainDispatcherRule
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class HistoricalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private lateinit var viewModel: HistoricalViewModel

    private fun makeEntity(id: String) = ViaryEntity(
        id = id,
        name = "Viagem $id",
        origin = "Origem",
        departureTime = Date(),
        initialBudget = 500.0,
        status = Viary.ViaryStatus.COMPLETED,
        finalBudget = 400.0,
        kmEnd = 120f,
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
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun `init - lista vazia, deve definir completedViary como vazio e isLoading false`() = runTest {
        every { repository.allCompleted } returns flowOf(emptyList())
        viewModel = HistoricalViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init
            assertTrue(state.completedViary.isEmpty())
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init - com viagens concluidas, deve popular completedViary`() = runTest {
        val entities = listOf(makeEntity("v1"), makeEntity("v2"))
        every { repository.allCompleted } returns flowOf(entities)
        viewModel = HistoricalViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init
            assertEquals(2, state.completedViary.size)
            assertEquals("Viagem v1", state.completedViary[0].name)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init - deve montar mapa lastCheckpoints com ultimo checkpoint por viary`() = runTest {
        val entity = makeEntity("v1")
        val checkpoint1 = Checkpoint(viaryId = "v1", placeName = "Parada 1")
        val checkpoint2 = Checkpoint(viaryId = "v1", placeName = "Parada 2")
        every { repository.allCompleted } returns flowOf(listOf(entity))
        coEvery { repository.getCheckpointsByViaryId("v1") } returns listOf(checkpoint1, checkpoint2)
        viewModel = HistoricalViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // estado inicial
            viewModel.init()
            val state = awaitItem() // estado pós-init
            assertEquals("Parada 2", state.lastCheckpoints["v1"]?.placeName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
