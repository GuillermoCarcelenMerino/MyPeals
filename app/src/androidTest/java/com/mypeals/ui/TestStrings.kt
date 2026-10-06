package com.mypeals.ui

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

/** Textos de la app en el idioma del dispositivo, para que los tests valgan en español e inglés. */
internal fun str(@StringRes id: Int, vararg args: Any): String =
    InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

internal fun plural(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
    InstrumentationRegistry.getInstrumentation().targetContext.resources
        .getQuantityString(id, quantity, *args)
