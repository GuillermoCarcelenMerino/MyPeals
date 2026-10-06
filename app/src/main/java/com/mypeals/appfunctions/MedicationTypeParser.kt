package com.mypeals.appfunctions

import com.mypeals.core.domain.model.MedicationType
import java.text.Normalizer

/**
 * Convierte la forma del medicamento dicha por voz ("cápsulas", "jarabe"...) en un
 * [MedicationType]. Sin coincidencia devuelve [MedicationType.PILL].
 */
fun parseMedicationType(text: String?): MedicationType {
    val normalized = Normalizer.normalize(text.orEmpty().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .trim()
    if (normalized.isEmpty()) return MedicationType.PILL
    return KEYWORDS.firstOrNull { (key, _) -> normalized.contains(key) }?.second ?: MedicationType.PILL
}

// El orden importa: "comprimido" debe comprobarse antes que términos más genéricos.
private val KEYWORDS: List<Pair<String, MedicationType>> = listOf(
    "capsula" to MedicationType.CAPSULE,
    "comprimido" to MedicationType.TABLET,
    "tableta" to MedicationType.TABLET,
    "sobre" to MedicationType.SACHET,
    "inyec" to MedicationType.INJECTION,
    "jeringa" to MedicationType.INJECTION,
    "jarabe" to MedicationType.SYRUP,
    "gota" to MedicationType.DROPS,
    "crema" to MedicationType.CREAM,
    "pomada" to MedicationType.CREAM,
    "inhalador" to MedicationType.INHALER,
    "parche" to MedicationType.PATCH,
    "supositorio" to MedicationType.SUPPOSITORY,
    "spray" to MedicationType.SPRAY,
    "pastilla" to MedicationType.PILL
)
