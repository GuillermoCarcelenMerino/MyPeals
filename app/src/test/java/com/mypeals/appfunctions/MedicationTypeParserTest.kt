package com.mypeals.appfunctions

import com.mypeals.core.domain.model.MedicationType
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationTypeParserTest {

    @Test
    fun `reconoce las formas habituales con y sin acentos`() {
        assertEquals(MedicationType.CAPSULE, parseMedicationType("cápsulas"))
        assertEquals(MedicationType.TABLET, parseMedicationType("Comprimido"))
        assertEquals(MedicationType.INJECTION, parseMedicationType("inyección"))
        assertEquals(MedicationType.SYRUP, parseMedicationType("jarabe"))
        assertEquals(MedicationType.DROPS, parseMedicationType("gotas"))
        assertEquals(MedicationType.SACHET, parseMedicationType("un sobre"))
    }

    @Test
    fun `sin texto o desconocido devuelve pastilla`() {
        assertEquals(MedicationType.PILL, parseMedicationType(null))
        assertEquals(MedicationType.PILL, parseMedicationType(""))
        assertEquals(MedicationType.PILL, parseMedicationType("algo raro"))
    }
}
