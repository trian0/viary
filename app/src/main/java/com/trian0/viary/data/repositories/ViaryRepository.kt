package com.trian0.viary.data.repositories

import android.content.Context
import android.net.Uri
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.saveImageToInternalStorage

class ViaryRepository(
    private val dao: ViaryDao,
    private val context: Context,
) {

    val viarys get() = dao.getAll()

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
)