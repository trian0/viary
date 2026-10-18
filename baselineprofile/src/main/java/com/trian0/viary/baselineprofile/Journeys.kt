package com.trian0.viary.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
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

/**
 * Fills the database through SeedBenchmarkDataReceiver (only present in benchmark variants).
 * Call it once from @Before: the broadcast starts the app process, so doing it inside a
 * measured iteration would turn a cold start into a warm one. `-f 32`
 * (FLAG_INCLUDE_STOPPED_PACKAGES) is needed because macrobenchmark force-stops the app.
 */
fun seedData() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val output = device.executeShellCommand(
        "am broadcast -f 32 -n $targetPackage/.benchmark.SeedBenchmarkDataReceiver"
    )
    check("result=-1" in output) { "Seeding broadcast failed: $output" }
}

fun MacrobenchmarkScope.waitForHome() {
    check(device.wait(Until.hasObject(By.res("nav_historical")), SPLASH_TIMEOUT_MS)) {
        "Home did not show the bottom navigation in time"
    }
}

fun MacrobenchmarkScope.openHistorical() {
    device.findObject(By.res("nav_historical")).click()
    check(device.wait(Until.hasObject(By.res("historical_item")), TIMEOUT_MS)) {
        "Historical list has no items; was the database seeded?"
    }
}

fun MacrobenchmarkScope.scrollHistorical() {
    val list = device.findObject(By.res("historical_list"))
    list.setGestureMargin(device.displayWidth / 5)
    repeat(3) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    repeat(3) {
        list.fling(Direction.UP)
        device.waitForIdle()
    }
}

fun MacrobenchmarkScope.openDetailsAndScroll() {
    device.findObject(By.res("historical_item")).click()
    check(device.wait(Until.hasObject(By.res("viary_details")), TIMEOUT_MS)) {
        "Viary details did not show up"
    }

    val details = device.findObject(By.res("viary_details"))
    details.setGestureMargin(device.displayWidth / 5)
    details.fling(Direction.DOWN)
    device.waitForIdle()

    device.pressBack()
    device.wait(Until.hasObject(By.res("historical_list")), TIMEOUT_MS)
}
