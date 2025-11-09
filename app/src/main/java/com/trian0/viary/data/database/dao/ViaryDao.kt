package com.trian0.viary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import kotlinx.coroutines.flow.Flow

@Dao
interface ViaryDao {
    @Query("SELECT * FROM ViaryEntity")
    fun getAll(): Flow<List<ViaryEntity>>

    @Query("SELECT * FROM ViaryEntity WHERE id = :id")
    fun getById(id: String): Flow<ViaryEntity>

    @Query("SELECT * FROM ViaryEntity WHERE status = :status LIMIT 1")
    suspend fun getByStatus(status: Viary.ViaryStatus): ViaryEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(viary: ViaryEntity)
}