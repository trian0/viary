package com.trian0.viary.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val viaryId: String = "",
    val placeName: String = "",
    val time: Date = Date(),
    val expense: Double = 0.0,
    val imageUri: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val images: String = "",
)