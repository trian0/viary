package com.trian0.viary

import android.util.Log
import app.cash.turbine.test
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.repositories.ViaryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ViaryRepository>(relaxed = true)
    private val httpClient = mockk<OkHttpClient>(relaxed = true)
    private lateinit var viewModel: MainViewModel

    private val fakeViaryEntity = ViaryEntity(
        id = "v1",
        name = "Em Andamento",
        origin = "Origem",
        departureTime = Date(),
        initialBudget = 0.0,
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
        every { Log.e(any(), any(), any()) } returns 0
        every { repository.viaryInProgress } returns flowOf(null)
    }

    @Test
    fun `navState - inicializa como Loading`() = runTest {
        viewModel = MainViewModel(repository, httpClient)

        assertEquals(SplashNavState.Loading, viewModel.navState.value)
    }

    @Test
    fun `navState - navega para Home apos o delay inicial`() = runTest {
        viewModel = MainViewModel(repository, httpClient)

        viewModel.navState.test {
            awaitItem() // Loading
            val next = awaitItem()
            assertEquals(SplashNavState.NavigateToHome, next)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `keepSplashOn - inicia como true enquanto Loading`() = runTest {
        viewModel = MainViewModel(repository, httpClient)

        assertTrue(viewModel.keepSplashOn.value)
    }

    @Test
    fun `hasViaryInProgress - false quando nao ha viary em andamento`() = runTest {
        every { repository.viaryInProgress } returns flowOf(null)
        viewModel = MainViewModel(repository, httpClient)

        // aguarda o flow ser coletado
        viewModel.navState.test {
            awaitItem() // Loading
            awaitItem() // NavigateToHome
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.hasViaryInProgress)
    }

    @Test
    fun `hasViaryInProgress - true quando ha viary em andamento`() = runTest {
        every { repository.viaryInProgress } returns flowOf(fakeViaryEntity)
        viewModel = MainViewModel(repository, httpClient)

        viewModel.navState.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.hasViaryInProgress)
    }
}
