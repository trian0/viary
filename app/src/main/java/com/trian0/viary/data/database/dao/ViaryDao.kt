package com.trian0.viary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.models.ViaryWithCheckpoints
import kotlinx.coroutines.flow.Flow

@Dao
interface ViaryDao {
    @Query("SELECT * FROM ViaryEntity")
    fun getAll(): Flow<List<ViaryEntity>>

    @Query("SELECT * FROM ViaryEntity WHERE id = :id")
    fun getById(id: String): Flow<ViaryEntity>

    @Query("SELECT COUNT(*) FROM ViaryEntity")
    suspend fun getCount(): Int

    @Query("SELECT * FROM ViaryEntity WHERE status = :status LIMIT 1")
    fun getByStatus(status: Viary.ViaryStatus): Flow<ViaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(viary: ViaryEntity)

    @Query("UPDATE ViaryEntity SET kmEnd = :distance WHERE id = :viaryId")
    suspend fun updateDistanceTraveled(viaryId: String, distance: Float)

    @Query("SELECT MAX(kmEnd) FROM ViaryEntity")
    suspend fun getGreaterDistance(): Float

    @Query("UPDATE ViaryEntity SET status = :status, latitudeArrival = :latitude, longitudeArrival = :longitude WHERE id = :viaryId")
    suspend fun finishViary(
        viaryId: String,
        status: Viary.ViaryStatus,
        latitude: Double,
        longitude: Double
    )

    @Transaction
    @Query("SELECT * FROM ViaryEntity WHERE id = :viaryId")
    suspend fun getViaryWithCheckpoints(viaryId: String): ViaryWithCheckpoints?

    @Transaction
    @Query("SELECT * FROM ViaryEntity WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getViaryInProgressWithCheckpoints(): Flow<ViaryWithCheckpoints?>

    @Query("SELECT * FROM viaryentity WHERE status = 'COMPLETED' ORDER BY departureTime DESC")
    fun getAllCompleted(): Flow<List<ViaryEntity>>
}