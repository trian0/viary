package com.trian0.viary.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import java.util.Date
import java.util.UUID

@Entity
data class ViaryEntity (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val origin: String,
    val departureTime: Date?,
    val initialBudget: Double,
    val status: Viary.ViaryStatus,
    val finalBudget: Double,
    val kmEnd: Float,
    val selectedImage: String?,
    val climate: String?,
    val latitudeOrigin: Double,
    val longitudeOrigin: Double,
    val latitudeArrival: Double,
    val longitudeArrival: Double
)