package com.trian0.viary.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HistoricalScrollBenchmarks {

    @Before
    fun seed() = seedData()

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollWithoutProfile() = scroll(CompilationMode.None())

    @Test
    fun scrollWithBaselineProfile() =
        scroll(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    private fun scroll(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = targetPackage,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        // COLD would kill the process after setupBlock, closing the Historical screen it opened
        startupMode = null,
        iterations = 10,
        setupBlock = {
            pressHome()
            grantRuntimePermissions()
            startActivityAndWait()
            waitForHome()
            openHistorical()
        },
    ) {
        scrollHistorical()
    }
}
