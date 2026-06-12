package com.trian0.viary

import android.app.Application
import com.trian0.viary.di.appModules
import com.trian0.viary.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class ViaryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this, "", WellKnownTileServer.MapLibre)
        startKoin {
            androidContext(this@ViaryApplication)
            modules(
                storageModule,
                appModules,
            )
        }
    }

}