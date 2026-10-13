package com.mypeals.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Parent of the screen ViewModels: holds the single immutable state [S] of the screen.
 * Screens read it from [uiState]; child ViewModels only change it through [publish].
 */
abstract class BaseViewModel<S>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /** State currently shown by the screen. */
    protected val currentState: S get() = _uiState.value

    /** Publishes a new state computed from the current one: `publish { it.copy(...) }`. */
    protected fun publish(reducer: (S) -> S) = _uiState.update(reducer)

    /**
     * Collects [flow] only while the screen observes [uiState] (and up to
     * [STOP_TIMEOUT_MILLIS] after it stops, so rotations do not restart it).
     */
    protected fun <T> observe(flow: Flow<T>, onEach: (T) -> Unit) {
        viewModelScope.launch {
            flow.whileSubscribed(_uiState.subscriptionCount, STOP_TIMEOUT_MILLIS).collect(onEach)
        }
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Emits the values of this flow only while [subscriptionCount] is above zero, with the same rule as
 * `SharingStarted.WhileSubscribed`: it starts on the first subscriber and stops [stopTimeoutMillis]
 * after the last one leaves. When it starts again, the flow is collected from the beginning.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> Flow<T>.whileSubscribed(
    subscriptionCount: StateFlow<Int>,
    stopTimeoutMillis: Long
): Flow<T> = SharingStarted.WhileSubscribed(stopTimeoutMillis)
    .command(subscriptionCount)
    .distinctUntilChanged()
    .flatMapLatest { command -> if (command == SharingCommand.START) this else emptyFlow() }
