package com.trian0.viary.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trian0.viary.data.database.ViaryDatabase
import com.trian0.viary.data.database.entities.CheckpointEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CheckpointDaoTest {

    private lateinit var db: ViaryDatabase
    private lateinit var dao: CheckpointDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ViaryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.checkpointDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun checkpoint(id: String, viaryId: String) = CheckpointEntity(
        id = id,
        viaryId = viaryId,
        placeName = "Local",
        time = Date(0),
        expense = 10.0,
    )

    @Test
    fun insert_and_getByViaryId() = runBlocking {
        dao.insert(checkpoint("cp1", "viary1"))
        val result = dao.getByViaryId("viary1")
        assertEquals(1, result.size)
        assertEquals("cp1", result[0].id)
    }

    @Test
    fun getByViaryId_returnsAllForSameViary() = runBlocking {
        dao.insert(checkpoint("cp1", "viary1"))
        dao.insert(checkpoint("cp2", "viary1"))
        dao.insert(checkpoint("cp3", "viary2"))
        val result = dao.getByViaryId("viary1")
        assertEquals(2, result.size)
    }

    @Test
    fun getByViaryId_returnsEmptyForUnknownId() = runBlocking {
        val result = dao.getByViaryId("nonexistent")
        assertTrue(result.isEmpty())
    }
}
