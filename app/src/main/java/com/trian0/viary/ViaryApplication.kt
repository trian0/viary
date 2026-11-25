package com.trian0.viary

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mapbox.common.MapboxOptions
import com.trian0.viary.di.appModules
import com.trian0.viary.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ViaryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        try {
            MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        } catch (e: Throwable) {
            isMapAvailable = false
            crashlytics.recordException(e)
        }

        startKoin {
            androidContext(this@ViaryApplication)
            modules(
                storageModule,
                appModules,
            )
        }
    }

    companion object {
        @Volatile
        var isMapAvailable = true
            private set
    }

}
