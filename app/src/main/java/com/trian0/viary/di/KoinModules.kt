package com.trian0.viary.di

import androidx.room.Room
import com.trian0.viary.data.database.ViaryDatabase
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.ui.create.CreateViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModules = module {
    single { ViaryRepository(get(), androidContext()) }
    viewModelOf(::CreateViewModel)
}

val storageModule = module {
    singleOf(::ViaryRepository)
    single {
        Room.databaseBuilder(
                context = androidContext(),
                klass = ViaryDatabase::class.java,
                name = "viary.db"
            ).fallbackToDestructiveMigration(false).build()
    }
    single {
        get<ViaryDatabase>().viaryDao()
    }
}