package com.trian0.viary.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @Before
    fun seed() = seedData()

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = targetPackage,
        includeInStartupProfile = true,
    ) {
        pressHome()
        grantRuntimePermissions()
        startActivityAndWait()
        waitForHome()
    }

    @Test
    fun historicalAndDetails() = rule.collect(
        packageName = targetPackage,
        includeInStartupProfile = false,
    ) {
        pressHome()
        grantRuntimePermissions()
        startActivityAndWait()
        waitForHome()
        openHistorical()
        scrollHistorical()
        openDetailsAndScroll()
    }
}
