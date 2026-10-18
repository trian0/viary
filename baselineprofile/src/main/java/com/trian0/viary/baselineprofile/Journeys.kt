package com.trian0.viary.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

private const val TIMEOUT_MS = 10_000L

// Splash stays up ~3s after loading, so the bottom bar showing up is the signal Home is ready
private const val SPLASH_TIMEOUT_MS = 15_000L

val targetPackage: String =
    InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: error("targetAppId not passed as instrumentation runner arg")

fun MacrobenchmarkScope.grantRuntimePermissions() {
    // Skips the location/notification prompts that would otherwise cover Home
    listOf(
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.POST_NOTIFICATIONS",
    ).forEach { device.executeShellCommand("pm grant $packageName $it") }
}

fun MacrobenchmarkScope.waitForHome() {
    check(device.wait(Until.hasObject(By.res("nav_historical")), SPLASH_TIMEOUT_MS)) {
        "Home did not show the bottom navigation in time"
    }
}

fun MacrobenchmarkScope.openHistoricalAndScroll() {
    device.findObject(By.res("nav_historical")).click()
    check(device.wait(Until.hasObject(By.res("historical_list")), TIMEOUT_MS)) {
        "Historical list did not show up"
    }

    val list = device.findObject(By.res("historical_list"))
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()

    device.findObject(By.res("nav_home")).click()
    device.wait(Until.hasObject(By.res("nav_historical")), TIMEOUT_MS)
}
