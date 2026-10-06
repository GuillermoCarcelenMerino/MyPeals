package com.mypeals.ui.common

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/** Texto con plural ("1 caja" / "3 cajas") a partir de un recurso `plurals`. */
@Composable
fun quantityStringResource(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
    LocalConfiguration.current // se recompone si cambia el idioma
    return LocalContext.current.resources.getQuantityString(id, quantity, *formatArgs)
}
