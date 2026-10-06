package com.mypeals.core.domain.model

/** Forma en la que se presenta/administra la medicación. Se guarda por nombre en Room. */
enum class MedicationType {
    PILL,
    CAPSULE,
    TABLET,
    SACHET,
    INJECTION,
    SYRUP,
    DROPS,
    CREAM,
    INHALER,
    PATCH,
    SUPPOSITORY,
    SPRAY,
    OTHER
}
