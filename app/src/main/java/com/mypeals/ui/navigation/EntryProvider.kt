package com.mypeals.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.mypeals.ui.Account.AccountNavigation
import com.mypeals.ui.Account.AccountScreen
import com.mypeals.ui.home.HomeNavigation
import com.mypeals.ui.home.HomeScreen

fun appEntryProvider(backStack: NavBackStack<NavKey>) = entryProvider<NavKey> {
    entry<HomeNavigation.Home> {

        HomeScreen({
            backStack.add(AccountNavigation.Profile)
        })

    }
    entry<AccountNavigation.Profile> {
        AccountScreen()
    }
}

