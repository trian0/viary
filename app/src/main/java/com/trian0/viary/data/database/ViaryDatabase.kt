package com.trian0.viary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trian0.viary.data.database.dao.ViaryDao
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.utils.Converters

@Database(entities = [ViaryEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class ViaryDatabase : RoomDatabase() {
    abstract fun viaryDao(): ViaryDao
}