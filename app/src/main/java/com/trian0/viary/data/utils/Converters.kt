package com.trian0.viary.data.utils

import androidx.room.TypeConverter
import com.trian0.viary.data.models.Viary
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromViaryStatus(value: Viary.ViaryStatus): String {
        return value.name
    }

    @TypeConverter
    fun toViaryStatus(value: String): Viary.ViaryStatus {
        return Viary.ViaryStatus.valueOf(value)
    }
}