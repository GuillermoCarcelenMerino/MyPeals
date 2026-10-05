package com.mypeals.core.domain.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Igual que [runCatching], pero relanza [CancellationException] para no romper
 * la cancelación estructurada de las corrutinas.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (exc: CancellationException) {
    throw exc
} catch (exc: Exception) {
    Result.failure(exc)
}
