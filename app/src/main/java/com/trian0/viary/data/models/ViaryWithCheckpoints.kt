package com.trian0.viary.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity

data class ViaryWithCheckpoints(
    @Embedded val viary: ViaryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "viaryId"
    )
    val checkpoints: List<CheckpointEntity>
)