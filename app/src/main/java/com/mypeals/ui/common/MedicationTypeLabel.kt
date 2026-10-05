package com.mypeals.ui.common

import androidx.annotation.StringRes
import com.mypeals.R
import com.mypeals.core.data.room.entities.MedicationType

@StringRes
fun MedicationType.labelRes(): Int = when (this) {
    MedicationType.PEAL -> R.string.medication_type_pill
    MedicationType.INJECTION -> R.string.medication_type_injection
}
