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
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN

        startKoin {
            androidContext(this@ViaryApplication)
            modules(
                storageModule,
                appModules,
            )
        }
    }

}