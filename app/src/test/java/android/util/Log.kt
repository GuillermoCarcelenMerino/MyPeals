package android.util

/**
 * Sustituye a `android.util.Log` en los tests de unidad: el `android.jar` de los tests no
 * implementa sus métodos y lanzaría "Method ... not mocked" en las ramas de error.
 */
@Suppress("unused", "UNUSED_PARAMETER")
object Log {
    @JvmStatic
    fun e(tag: String?, msg: String?, tr: Throwable?): Int = 0

    @JvmStatic
    fun e(tag: String?, msg: String?): Int = 0
}
