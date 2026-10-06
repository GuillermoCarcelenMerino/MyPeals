package com.mypeals

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mypeals.core.data.room.AppDataBase
import com.mypeals.core.data.room.migrations.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDataBase::class.java
    )

    @Test
    fun migrate1To2_conservaLosDatosYConvierteLosTipos() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO peals (id, peal_name, quantity, type) VALUES (1, 'Ibuprofeno', 20, 'PEAL')")
            execSQL("INSERT INTO peals (id, peal_name, quantity, type) VALUES (2, 'Insulina', 0, 'INJECTION')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query(
            "SELECT id, name, type, units_per_box, remaining_units, active " +
                "FROM medications ORDER BY id"
        ).use { cursor ->
            assertTrue(cursor.moveToNext())
            assertEquals(1, cursor.getInt(0))
            assertEquals("Ibuprofeno", cursor.getString(1))
            assertEquals("PILL", cursor.getString(2))
            assertEquals(20, cursor.getInt(3))
            assertEquals(20, cursor.getInt(4))
            assertEquals(0, cursor.getInt(5))

            assertTrue(cursor.moveToNext())
            assertEquals(2, cursor.getInt(0))
            assertEquals("INJECTION", cursor.getString(2))
            assertTrue(cursor.isNull(3))
            assertEquals(0, cursor.getInt(4))

            assertFalse(cursor.moveToNext())
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
