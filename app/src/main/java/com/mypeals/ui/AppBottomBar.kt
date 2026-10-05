package com.mypeals.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mypeals.ui.navigation.TopLevelDestination

@Composable
fun AppBottomBar(
    destinations: List<TopLevelDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        destinations.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onDestinationSelected(index) },
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(destination.label)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}
