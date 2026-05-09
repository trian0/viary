package com.trian0.viary.data.repositories

import android.content.Context
import android.net.Uri
import com.trian0.viary.data.database.dao.CheckpointDao
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.models.ViaryWithCheckpoints
import com.trian0.viary.data.utils.saveImageToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ViaryRepository(
    private val dao: ViaryDao,
    private val checkpointDao: CheckpointDao,
    private val context: Context,
) {
    val viaryInProgress get() = dao.getByStatus(Viary.ViaryStatus.IN_PROGRESS)

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

    suspend fun getCheckpointsByViaryId(viaryId: String): List<Checkpoint> {
        return checkpointDao.getByViaryId(viaryId).map { it.toCheckpoint() }
    }

    suspend fun saveCheckpoint(checkpoint: Checkpoint, coverUri: Uri?, imagesUri: List<Uri>) {
        var finalImagePath: String? = null

        coverUri?.let {
            finalImagePath = saveImageToInternalStorage(context, it)
        }
        val savedPaths = imagesUri.mapNotNull { uri ->
            saveImageToInternalStorage(context, uri)
        }
        val entity = checkpoint.toEntity().copy(
            imageUri = finalImagePath,
            images = savedPaths.joinToString(",")
        )

        checkpointDao.insert(entity)
    }

    val viaryInProgressWithCheckpoints get() = dao.getViaryInProgressWithCheckpoints()
}

fun Viary.toViaryEntity() = ViaryEntity(
    id = this.id,
    name = this.name,
    status = this.status,
    origin = this.origin,
    departureTime = this.departureTime,
    initialBudget = this.initialBudget,
    finalBudget = this.finalBudget,
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
    initialBudget = this.initialBudget,
    finalBudget = this.finalBudget,
    kmEnd = this.kmEnd,
    selectedImage = this.selectedImage,
    climate = this.climate,
    latitudeOrigin = this.latitudeOrigin,
    longitudeOrigin = this.longitudeOrigin,
    latitudeArrival = this.latitudeArrival,
    longitudeArrival = this.longitudeArrival
)

fun CheckpointEntity.toCheckpoint() = Checkpoint(
    id = this.id,
    viaryId = this.viaryId,
    placeName = this.placeName,
    time = this.time,
    expense = this.expense,
    imageUri = this.imageUri,
    latitude = this.latitude,
    longitude = this.longitude,
    images = this.images.split(",").filter { it.isNotEmpty() }
)

fun Checkpoint.toEntity() = CheckpointEntity(
    id = this.id,
    viaryId = this.viaryId,
    placeName = this.placeName,
    time = this.time,
    expense = this.expense,
    imageUri = this.imageUri,
    latitude = this.latitude,
    longitude = this.longitude,
    images = images.joinToString(",")
)

fun ViaryWithCheckpoints.toViary() = viary.toViary().copy(
    checkpoints = checkpoints.map { it.toCheckpoint() }
)