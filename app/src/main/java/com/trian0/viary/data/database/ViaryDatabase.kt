package com.trian0.viary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trian0.viary.data.database.dao.CheckpointDao
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.utils.Converters

@Database(
    entities = [
        ViaryEntity::class,
        CheckpointEntity::class, ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ViaryDatabase : RoomDatabase() {
    abstract fun viaryDao(): ViaryDao
    abstract fun checkpointDao(): CheckpointDao
}