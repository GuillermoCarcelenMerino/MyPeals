package com.mypeals.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mypeals.core.data.repository.MedicationRepositoryImpl
import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.domain.model.Frequency
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.IntakeStatus
import com.mypeals.core.domain.model.Medication
import com.mypeals.core.domain.model.MedicationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationRepositoryTest {

    private lateinit var database: AppDataBase
    private lateinit var repository: MedicationRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        repository = MedicationRepositoryImpl(database)
    }

    @After
    fun tearDown() = database.close()

    private fun medication(units: Int = 10, dose: Int = 2) = Medication(
        name = "Ibuprofeno",
        type = MedicationType.entries.first(),
        unitsPerBox = 10,
        remainingUnits = units,
        dosePerIntake = dose,
        frequency = Frequency(8, FrequencyUnit.HOURS),
        firstIntakeAtMillis = 0L
    )

    @Test
    fun añadirYObservar() = runBlocking {
        val id = repository.addMedication(medication())

        val list = repository.observeMedications().first()
        assertEquals(listOf(id), list.map { it.id })
        assertEquals("Ibuprofeno", list.single().name)
    }

    @Test
    fun editar_actualizaLaFila() = runBlocking {
        val id = repository.addMedication(medication())

        repository.updateMedication(repository.getMedication(id)!!.copy(name = "Paracetamol", active = false))

        val saved = repository.getMedication(id)!!
        assertEquals("Paracetamol", saved.name)
        assertEquals(false, saved.active)
    }

    @Test
    fun tomada_descuentaLaDosisYLaAnotaEnElHistorial() = runBlocking {
        val id = repository.addMedication(medication(units = 10, dose = 2))

        repository.registerIntake(id, scheduledAtMillis = 100L, atMillis = 150L, status = IntakeStatus.TAKEN)

        assertEquals(8, repository.getMedication(id)!!.remainingUnits)
        val log = database.intakeLogDao().observeByMedication(id).first().single()
        assertEquals(IntakeStatus.TAKEN, log.status)
        assertEquals(150L, log.takenAt)
    }

    @Test
    fun tomada_noBajaDeCero() = runBlocking {
        val id = repository.addMedication(medication(units = 1, dose = 2))

        repository.registerIntake(id, 100L, 150L, IntakeStatus.TAKEN)

        assertEquals(0, repository.getMedication(id)!!.remainingUnits)
    }

    @Test
    fun pospuesta_noDescuentaStock() = runBlocking {
        val id = repository.addMedication(medication(units = 10))

        repository.registerIntake(id, 100L, 150L, IntakeStatus.SNOOZED)

        assertEquals(10, repository.getMedication(id)!!.remainingUnits)
        assertNull(database.intakeLogDao().observeByMedication(id).first().single().takenAt)
    }

    @Test
    fun borrar_eliminaTambienElHistorial() = runBlocking {
        val id = repository.addMedication(medication())
        repository.registerIntake(id, 100L, 150L, IntakeStatus.TAKEN)

        repository.deleteMedication(repository.getMedication(id)!!)

        assertNull(repository.getMedication(id))
        assertEquals(0, database.intakeLogDao().observeByMedication(id).first().size)
    }
}
