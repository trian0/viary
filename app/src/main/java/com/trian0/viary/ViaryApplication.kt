package com.trian0.viary

import android.app.Application
import com.trian0.viary.di.appModules
import com.trian0.viary.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ViaryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ViaryApplication)
            modules(
                storageModule,
                appModules,
            )
        }
    }

}