package com.trian0.viary.di

import androidx.room.Room
import com.trian0.viary.MainViewModel
import com.trian0.viary.data.database.MIGRATION_1_2
import com.trian0.viary.data.database.ViaryDatabase
import com.trian0.viary.data.repositories.ViaryRepository
import com.trian0.viary.helpers.LocationHelper
import com.trian0.viary.ui.checkpoint.CheckpointViewModel
import com.trian0.viary.ui.create.CreateViewModel
import com.trian0.viary.ui.historical.HistoricalViewModel
import com.trian0.viary.ui.home.HomeViewModel
import com.trian0.viary.ui.viarydetails.ViaryDetailsViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModules = module {
    single { LocationHelper(androidContext()) }
    single { OkHttpClient() }
    viewModelOf(::CreateViewModel)
    viewModelOf(::CheckpointViewModel)
    viewModel { HomeViewModel(get(), get(), Dispatchers.IO) }
    viewModel { HistoricalViewModel(get(), Dispatchers.IO) }
    viewModel { ViaryDetailsViewModel(get(), Dispatchers.IO) }
    viewModel { MainViewModel(get(), get(), Dispatchers.IO) }
}

val storageModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = ViaryDatabase::class.java,
            name = "viary.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
    single { get<ViaryDatabase>().viaryDao() }
    single { get<ViaryDatabase>().checkpointDao() }
    single { ViaryRepository(get(), get(), androidContext()) }
}