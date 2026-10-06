package com.mypeals.core.domain.model

/** Unidad de la frecuencia de toma: "cada [interval] horas / días / semanas". */
enum class FrequencyUnit {
    HOURS,
    DAYS,
    WEEKS
}

/** Días de la semana (de lunes a domingo). Se usan en las frecuencias semanales. */
enum class Weekday {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}
