package com.mypeals.ui.common

import androidx.annotation.StringRes
import com.mypeals.R
import com.mypeals.core.domain.model.MedicationType

@StringRes
fun MedicationType.labelRes(): Int = when (this) {
    MedicationType.PILL -> R.string.medication_type_pill
    MedicationType.CAPSULE -> R.string.medication_type_capsule
    MedicationType.TABLET -> R.string.medication_type_tablet
    MedicationType.SACHET -> R.string.medication_type_sachet
    MedicationType.INJECTION -> R.string.medication_type_injection
    MedicationType.SYRUP -> R.string.medication_type_syrup
    MedicationType.DROPS -> R.string.medication_type_drops
    MedicationType.CREAM -> R.string.medication_type_cream
    MedicationType.INHALER -> R.string.medication_type_inhaler
    MedicationType.PATCH -> R.string.medication_type_patch
    MedicationType.SUPPOSITORY -> R.string.medication_type_suppository
    MedicationType.SPRAY -> R.string.medication_type_spray
    MedicationType.OTHER -> R.string.medication_type_other
}
