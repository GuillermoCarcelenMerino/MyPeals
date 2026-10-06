package com.mypeals.ui.common

import androidx.annotation.StringRes
import com.mypeals.R
import com.mypeals.core.domain.model.FrequencyUnit
import com.mypeals.core.domain.model.Weekday

@StringRes
fun FrequencyUnit.labelRes(): Int = when (this) {
    FrequencyUnit.HOURS -> R.string.frequency_hours
    FrequencyUnit.DAYS -> R.string.frequency_days
    FrequencyUnit.WEEKS -> R.string.frequency_weeks
}

@StringRes
fun Weekday.shortLabelRes(): Int = when (this) {
    Weekday.MONDAY -> R.string.weekday_mon
    Weekday.TUESDAY -> R.string.weekday_tue
    Weekday.WEDNESDAY -> R.string.weekday_wed
    Weekday.THURSDAY -> R.string.weekday_thu
    Weekday.FRIDAY -> R.string.weekday_fri
    Weekday.SATURDAY -> R.string.weekday_sat
    Weekday.SUNDAY -> R.string.weekday_sun
}
