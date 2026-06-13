package com.trian0.viary.data.repositories

import com.trian0.viary.data.database.entities.CheckpointEntity
import com.trian0.viary.data.database.entities.ViaryEntity
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.models.ViaryWithCheckpoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ViaryRepositoryMappersTest {

    private val sampleDate = Date(1_700_000_000_000L)

    private val sampleViary = Viary(
        id = "v-abc",
        name = "Viagem ao Nordeste",
        origin = "Recife",
        departureTime = sampleDate,
        initialBudget = 3000.0,
        status = Viary.ViaryStatus.IN_PROGRESS,
        finalBudget = 2500.0,
        kmEnd = 850f,
        selectedImage = "/images/cover.jpg",
        climate = "SUNNY",
        latitudeOrigin = -8.05,
        longitudeOrigin = -34.88,
        latitudeArrival = -3.73,
        longitudeArrival = -38.52,
    )

    private val sampleViaryEntity = ViaryEntity(
        id = "v-abc",
        name = "Viagem ao Nordeste",
        origin = "Recife",
        departureTime = sampleDate,
        initialBudget = 3000.0,
        status = Viary.ViaryStatus.IN_PROGRESS,
        finalBudget = 2500.0,
        kmEnd = 850f,
        selectedImage = "/images/cover.jpg",
        climate = "SUNNY",
        latitudeOrigin = -8.05,
        longitudeOrigin = -34.88,
        latitudeArrival = -3.73,
        longitudeArrival = -38.52,
    )

    private val sampleCheckpoint = Checkpoint(
        id = "cp-1",
        viaryId = "v-abc",
        placeName = "Fortaleza",
        time = sampleDate,
        expense = 150.0,
        imageUri = "cover_cp.jpg",
        latitude = -3.73,
        longitude = -38.52,
        images = listOf("a.jpg", "b.jpg"),
    )

    private val sampleCheckpointEntity = CheckpointEntity(
        id = "cp-1",
        viaryId = "v-abc",
        placeName = "Fortaleza",
        time = sampleDate,
        expense = 150.0,
        imageUri = "cover_cp.jpg",
        latitude = -3.73,
        longitude = -38.52,
        images = "a.jpg,b.jpg",
    )

    @Test
    fun `Viary toViaryEntity - deve mapear todos os campos corretamente`() {
        val entity = sampleViary.toViaryEntity()

        assertEquals(sampleViary.id, entity.id)
        assertEquals(sampleViary.name, entity.name)
        assertEquals(sampleViary.origin, entity.origin)
        assertEquals(sampleViary.departureTime, entity.departureTime)
        assertEquals(sampleViary.initialBudget, entity.initialBudget, 0.0)
        assertEquals(sampleViary.status, entity.status)
        assertEquals(sampleViary.finalBudget, entity.finalBudget, 0.0)
        assertEquals(sampleViary.kmEnd, entity.kmEnd)
        assertEquals(sampleViary.selectedImage, entity.selectedImage)
        assertEquals(sampleViary.climate, entity.climate)
        assertEquals(sampleViary.latitudeOrigin, entity.latitudeOrigin, 0.0)
        assertEquals(sampleViary.longitudeOrigin, entity.longitudeOrigin, 0.0)
        assertEquals(sampleViary.latitudeArrival, entity.latitudeArrival, 0.0)
        assertEquals(sampleViary.longitudeArrival, entity.longitudeArrival, 0.0)
    }

    @Test
    fun `ViaryEntity toViary - deve mapear todos os campos corretamente`() {
        val viary = sampleViaryEntity.toViary()

        assertEquals(sampleViaryEntity.id, viary.id)
        assertEquals(sampleViaryEntity.name, viary.name)
        assertEquals(sampleViaryEntity.origin, viary.origin)
        assertEquals(sampleViaryEntity.departureTime, viary.departureTime)
        assertEquals(sampleViaryEntity.initialBudget, viary.initialBudget, 0.0)
        assertEquals(sampleViaryEntity.status, viary.status)
        assertEquals(sampleViaryEntity.finalBudget, viary.finalBudget, 0.0)
        assertEquals(sampleViaryEntity.kmEnd, viary.kmEnd)
        assertEquals(sampleViaryEntity.selectedImage, viary.selectedImage)
        assertEquals(sampleViaryEntity.climate, viary.climate)
        assertTrue(viary.checkpoints.isEmpty()) // entidade não contém checkpoints
    }

    @Test
    fun `Checkpoint toEntity - deve serializar images como string separada por virgula`() {
        val entity = sampleCheckpoint.toEntity()

        assertEquals(sampleCheckpoint.id, entity.id)
        assertEquals(sampleCheckpoint.viaryId, entity.viaryId)
        assertEquals(sampleCheckpoint.placeName, entity.placeName)
        assertEquals(sampleCheckpoint.expense, entity.expense, 0.0)
        assertEquals(sampleCheckpoint.imageUri, entity.imageUri)
        assertEquals("a.jpg,b.jpg", entity.images)
    }

    @Test
    fun `CheckpointEntity toCheckpoint - deve desserializar images de string para lista`() {
        val checkpoint = sampleCheckpointEntity.toCheckpoint()

        assertEquals(sampleCheckpointEntity.id, checkpoint.id)
        assertEquals(sampleCheckpointEntity.viaryId, checkpoint.viaryId)
        assertEquals(sampleCheckpointEntity.placeName, checkpoint.placeName)
        assertEquals(2, checkpoint.images.size)
        assertEquals("a.jpg", checkpoint.images[0])
        assertEquals("b.jpg", checkpoint.images[1])
        assertEquals(sampleCheckpointEntity.imageUri, checkpoint.imageUri)
    }

    @Test
    fun `CheckpointEntity toCheckpoint - quando images esta vazio, deve retornar lista vazia`() {
        val entity = sampleCheckpointEntity.copy(images = "")
        val checkpoint = entity.toCheckpoint()

        assertTrue(checkpoint.images.isEmpty())
    }

    @Test
    fun `ViaryWithCheckpoints toViary - deve incluir checkpoints convertidos no Viary`() {
        val relation = ViaryWithCheckpoints(
            viary = sampleViaryEntity,
            checkpoints = listOf(sampleCheckpointEntity),
        )

        val viary = relation.toViary()

        assertEquals(sampleViaryEntity.id, viary.id)
        assertEquals(1, viary.checkpoints.size)
        assertEquals("Fortaleza", viary.checkpoints[0].placeName)
        assertEquals(listOf("a.jpg", "b.jpg"), viary.checkpoints[0].images)
    }
}
