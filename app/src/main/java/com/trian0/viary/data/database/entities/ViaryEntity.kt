package com.trian0.viary.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trian0.viary.data.models.Viary
import java.util.Date
import java.util.UUID

@Entity
data class ViaryEntity (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val origin: String,
    val departureTime: Date?,
    val kmStart: Double,
    val status: Viary.ViaryStatus,
    val kmEnd: Double,
    val selectedImage: String?,
)