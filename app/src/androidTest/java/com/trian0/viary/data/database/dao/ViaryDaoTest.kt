package com.trian0.viary.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trian0.viary.data.database.ViaryDatabase
import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Viary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ViaryDaoTest {

    private lateinit var db: ViaryDatabase
    private lateinit var dao: ViaryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ViaryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.viaryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun entity(
        id: String,
        name: String = "Viagem",
        status: Viary.ViaryStatus = Viary.ViaryStatus.COMPLETED,
        kmEnd: Float = 0f,
        latitudeArrival: Double = 0.0,
        longitudeArrival: Double = 0.0,
    ) = ViaryEntity(
        id = id,
        name = name,
        origin = "Origem",
        departureTime = Date(0),
        initialBudget = 100.0,
        status = status,
        finalBudget = 100.0,
        kmEnd = kmEnd,
        selectedImage = null,
        climate = null,
        latitudeOrigin = 0.0,
        longitudeOrigin = 0.0,
        latitudeArrival = latitudeArrival,
        longitudeArrival = longitudeArrival,
    )

    @Test
    fun save_and_getById() = runBlocking {
        val e = entity(id = "id1")
        dao.save(e)
        val result = dao.getById("id1").first()
        assertEquals(e, result)
    }

    @Test
    fun getAll_returnsAllSaved() = runBlocking {
        dao.save(entity(id = "a"))
        dao.save(entity(id = "b"))
        val result = dao.getAll().first()
        assertEquals(2, result.size)
    }

    @Test
    fun getByStatus_returnsOnlyInProgress() = runBlocking {
        dao.save(entity(id = "in", status = Viary.ViaryStatus.IN_PROGRESS))
        dao.save(entity(id = "done", status = Viary.ViaryStatus.COMPLETED))
        val result = dao.getByStatus(Viary.ViaryStatus.IN_PROGRESS).first()
        assertNotNull(result)
        assertEquals("in", result!!.id)
    }

    @Test
    fun updateDistanceTraveled_updatesKmEnd() = runBlocking {
        dao.save(entity(id = "trip1"))
        dao.updateDistanceTraveled("trip1", 42.5f)
        val result = dao.getById("trip1").first()
        assertEquals(42.5f, result.kmEnd, 0.001f)
    }

    @Test
    fun getGreaterDistance_returnsMax() = runBlocking {
        dao.save(entity(id = "a", kmEnd = 10f))
        dao.save(entity(id = "b", kmEnd = 50f))
        dao.save(entity(id = "c", kmEnd = 30f))
        val result = dao.getGreaterDistance()
        assertEquals(50f, result, 0.001f)
    }

    @Test
    fun finishViary_updatesStatusAndCords() = runBlocking {
        dao.save(entity(id = "trip", status = Viary.ViaryStatus.IN_PROGRESS))
        dao.finishViary("trip", Viary.ViaryStatus.COMPLETED, 10.0, 20.0)
        val result = dao.getById("trip").first()
        assertEquals(Viary.ViaryStatus.COMPLETED, result.status)
        assertEquals(10.0, result.latitudeArrival, 0.001)
        assertEquals(20.0, result.longitudeArrival, 0.001)
    }

    @Test
    fun getAllCompleted_excludesInProgress() = runBlocking {
        dao.save(entity(id = "in", status = Viary.ViaryStatus.IN_PROGRESS))
        dao.save(entity(id = "done1", status = Viary.ViaryStatus.COMPLETED))
        dao.save(entity(id = "done2", status = Viary.ViaryStatus.COMPLETED))
        val result = dao.getAllCompleted().first()
        assertEquals(2, result.size)
        assertTrue(result.none { it.status == Viary.ViaryStatus.IN_PROGRESS })
    }

    @Test
    fun getViaryWithCheckpoints_includesRelated() = runBlocking {
        dao.save(entity(id = "viary1"))
        db.checkpointDao().insert(
            CheckpointEntity(
                id = "cp1",
                viaryId = "viary1",
                placeName = "Parada",
                time = Date(0),
                expense = 50.0,
            )
        )
        val result = dao.getViaryWithCheckpoints("viary1")
        assertNotNull(result)
        assertEquals(1, result!!.checkpoints.size)
        assertEquals("cp1", result.checkpoints[0].id)
    }
}
