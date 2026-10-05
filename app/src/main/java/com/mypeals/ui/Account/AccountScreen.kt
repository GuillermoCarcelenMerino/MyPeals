package com.mypeals.ui.Account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AccountScreen() {
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {

        Button(onClick = {}) {
            Text(text = "Cuenta", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    AccountScreen()
}