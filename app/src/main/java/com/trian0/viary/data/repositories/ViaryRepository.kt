package com.trian0.viary.data.repositories

import android.content.Context
import android.location.Location
import android.net.Uri
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.saveImageToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.withContext

class ViaryRepository(
    private val dao: ViaryDao,
    private val context: Context,
) {
    suspend fun getViaryInProgress() = withContext(Dispatchers.IO) {
        dao.getByStatus(Viary.ViaryStatus.IN_PROGRESS)
    }

    suspend fun create(viary: Viary, imageUri: Uri?) {
        var finalImagePath: String? = null

        imageUri?.let {
            finalImagePath = saveImageToInternalStorage(context, it)
        }

        val entity = viary.toViaryEntity().copy(selectedImage = finalImagePath)
        dao.save(entity)
    }

    suspend fun updateStatus(viary: Viary, status: Viary.ViaryStatus) {
        val entity = viary.copy(
            status = status
        ).toViaryEntity()
        dao.save(entity)
    }

    suspend fun updateDistanceTraveled(viaryId: String, distance: Float) {
        dao.updateDistanceTraveled(viaryId, distance)
    }

    suspend fun getTotalViary() = withContext(Dispatchers.IO) {
        dao.getCount()
    }

    suspend fun getGreaterDistance() = withContext(Dispatchers.IO) {
        dao.getGreaterDistance()
    }

    suspend fun finishViary(viaryId: String, status: Viary.ViaryStatus, latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        dao.finishViary(viaryId, status, latitude, longitude)
    }
}

fun Viary.toViaryEntity() = ViaryEntity(
    id = this.id,
    name = this.name,
    status = this.status,
    origin = this.origin,
    departureTime = this.departureTime,
    kmStart = this.kmStart,
    kmEnd = this.kmEnd,
    selectedImage = this.selectedImage,
    climate = this.climate,
    latitudeOrigin = this.latitudeOrigin,
    longitudeOrigin = this.longitudeOrigin,
    latitudeArrival = this.latitudeArrival,
    longitudeArrival = this.longitudeArrival
)

fun ViaryEntity.toViary() = Viary(
    id = this.id,
    name = this.name,
    status = this.status,
    origin = this.origin,
    departureTime = this.departureTime,
    kmStart = this.kmStart,
    kmEnd = this.kmEnd,
    selectedImage = this.selectedImage,
    climate = this.climate,
    latitudeOrigin = this.latitudeOrigin,
    longitudeOrigin = this.longitudeOrigin,
    latitudeArrival = this.latitudeArrival,
    longitudeArrival = this.longitudeArrival
)