package com.trian0.viary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trian0.viary.data.database.entities.CheckpointEntity

@Dao
interface CheckpointDao {

    @Query("SELECT * FROM checkpoints WHERE viaryId = :viaryId")
    suspend fun getByViaryId(viaryId: String): List<CheckpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkpoint: CheckpointEntity)
}