package com.trian0.viary.benchmark

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trian0.viary.data.database.dao.CheckpointDao
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Fills the database with completed viaries so baseline profile journeys and benchmarks
 * have a populated Home, Historical list and details screen. Fixed ids + REPLACE make it
 * safe to trigger before every iteration.
 *
 * adb shell am broadcast -n com.trian0.viary/.benchmark.SeedBenchmarkDataReceiver
 */
class SeedBenchmarkDataReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val koin = GlobalContext.get()
        val viaryDao = koin.get<ViaryDao>()
        val checkpointDao = koin.get<CheckpointDao>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                seed(viaryDao, checkpointDao)
                pending.resultCode = Activity.RESULT_OK
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun seed(viaryDao: ViaryDao, checkpointDao: CheckpointDao) {
        val now = System.currentTimeMillis()

        repeat(VIARY_COUNT) { index ->
            val origin = CITIES[index % CITIES.size]
            val destination = CITIES[(index + 3) % CITIES.size]
            val departure = now - TimeUnit.DAYS.toMillis((index + 1) * 7L)
            val viaryId = "benchmark-viary-$index"

            viaryDao.save(
                ViaryEntity(
                    id = viaryId,
                    name = "Viagem ${origin.name} → ${destination.name}",
                    origin = origin.name,
                    departureTime = Date(departure),
                    initialBudget = 1500.0 + index * 50,
                    status = Viary.ViaryStatus.COMPLETED,
                    finalBudget = 900.0 + index * 20,
                    kmEnd = 120f + index * 35f,
                    selectedImage = null,
                    climate = Viary.ViaryClimate.entries[index % Viary.ViaryClimate.entries.size].name,
                    latitudeOrigin = origin.latitude,
                    longitudeOrigin = origin.longitude,
                    latitudeArrival = destination.latitude,
                    longitudeArrival = destination.longitude,
                    arrivalTime = Date(departure + TimeUnit.HOURS.toMillis(10)),
                )
            )

            val checkpointCount = 2 + index % 3
            repeat(checkpointCount) { step ->
                val fraction = (step + 1).toDouble() / (checkpointCount + 1)
                checkpointDao.insert(
                    CheckpointEntity(
                        id = "$viaryId-checkpoint-$step",
                        viaryId = viaryId,
                        placeName = "Parada ${step + 1}",
                        time = Date(departure + TimeUnit.HOURS.toMillis(2L * (step + 1))),
                        expense = 40.0 + step * 25,
                        latitude = origin.latitude + (destination.latitude - origin.latitude) * fraction,
                        longitude = origin.longitude + (destination.longitude - origin.longitude) * fraction,
                    )
                )
            }
        }
    }

    private data class City(val name: String, val latitude: Double, val longitude: Double)

    private companion object {
        const val VIARY_COUNT = 30

        val CITIES = listOf(
            City("São Paulo", -23.5505, -46.6333),
            City("Rio de Janeiro", -22.9068, -43.1729),
            City("Belo Horizonte", -19.9167, -43.9345),
            City("Curitiba", -25.4284, -49.2733),
            City("Florianópolis", -27.5954, -48.5480),
            City("Salvador", -12.9777, -38.5016),
            City("Brasília", -15.7939, -47.8828),
            City("Porto Alegre", -30.0346, -51.2177),
        )
    }
}
