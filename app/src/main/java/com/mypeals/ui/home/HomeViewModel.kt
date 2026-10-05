package com.mypeals.ui.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.domain.usercase.AddPealsUseCase
import com.mypeals.core.domain.usercase.GetPealsUseCase
import com.mypeals.core.domain.usercase.UpdatePealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.toList

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPealsUseCase: GetPealsUseCase,
    private val addPealsUseCase: AddPealsUseCase,
    private val updatePealUseCase: UpdatePealUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())

    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        getAllPeals()
    }

    fun addPeal(peal: Peal) {
        viewModelScope.launch {
            addPealsUseCase(peal).onSuccess {
                Log.d("TAG", "getAllPeals: saxdas")

            }.onFailure {
                Log.d("TAG", "getAllPeals: saxdas")

            }
        }
    }

    private fun getAllPeals() {
        viewModelScope.launch {
            getPealsUseCase().onSuccess {
                //todo add logs
                _uiState.value = _uiState.value.copy(peals = it)
            }.onFailure {
                //todo add logs
                Log.d("TAG", "getAllPeals: szadsa")
            }

        }
    }

    fun updatePeal(peal: Peal, quantity: Int) {
        viewModelScope.launch {
            updatePealUseCase(peal).onSuccess {
                //todo add logs
                _uiState.value = _uiState.value.copy(
                    peals = _uiState.value.peals.map { currentPeal ->
                        if (currentPeal.id == peal.id) {
                            currentPeal.copy(quantity = quantity)
                        } else {
                            currentPeal
                        }
                    }
                )

            }.onFailure {
                //todo add logs
                Log.d("TAG", "getAllPeals: szadsa")
            }

        }
    }
}