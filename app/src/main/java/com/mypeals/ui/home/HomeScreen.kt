package com.mypeals.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypeals.R
import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.data.room.entities.PealType
import kotlinx.coroutines.launch
import kotlin.Int
import kotlin.Unit

@Composable
fun HomeScreen(navigateAccount: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    Home(
        navigateAccount,
        state.value,
        { peal -> viewModel.addPeal(peal) },
        { peal, quantity -> viewModel.updatePeal(peal, quantity) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navigateAccount: () -> Unit,
    state: UIState,
    function: (peal: Peal) -> Unit,
    updateQuantity: (peal: Peal, quantity: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }

    if (!state.loading) {
        ListPeals(
            state = state,
            onPealClick = {
                showBottomSheet = true
                scope.launch { sheetState.partialExpand() }
            },
            updateQuantity,
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            PealBottomSheetContent(function)
        }
    }
}

@Composable
fun PealBottomSheetContent(function: (peal: Peal) -> Unit) {

    Column {
        Button(onClick = {
            function(Peal(0, "pastilla1", 3, PealType.INJECTION))
        }) {
            Text("CLICA PARA CREAR LA PASTILLA")
        }
    }
}

@Composable
fun ListPeals(state: UIState, onPealClick: () -> Unit, updateQuantity: (Peal, Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(state.peals) { id, peal ->
            Card(onClick = {}) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(peal.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary
                                )

                        ) {
                            Button(shape = MaterialTheme.shapes.extraLarge, onClick = {
                                updateQuantity(peal, peal.quantity - 1)
                            }, enabled = peal.quantity >= 1) {
                                Icon(
                                    painter = painterResource(R.drawable.menos_icon),
                                    tint = MaterialTheme.colorScheme.secondary,
                                    contentDescription = "minus peals",
                                    modifier = Modifier
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                peal.quantity.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )

                            Button(shape = MaterialTheme.shapes.extraLarge, onClick = {
                                updateQuantity(peal, peal.quantity + 1)
                            }) {

                                Icon(
                                    painter = painterResource(R.drawable.sumar_icon),
                                    tint = Color.White,
                                    contentDescription = "plus peals"
                                )

                            }

                        }

                    }

                }
            }

        }
        item {
            Button(onClick = { onPealClick() }) {
                Text("Añadir nuevo farmaco ")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    Home({}, UIState(peals = listOf(Peal(3, "", 3, PealType.INJECTION))), {}, {_,_ -> })
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPealBottomSheetContent() {
    PealBottomSheetContent({})
}