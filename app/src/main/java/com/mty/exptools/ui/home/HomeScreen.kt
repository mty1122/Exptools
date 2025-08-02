package com.mty.exptools.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mty.exptools.ui.home.bottom.HomeBottomBar
import com.mty.exptools.ui.home.center.list.ListScreen
import com.mty.exptools.ui.home.center.more.MoreScreen
import com.mty.exptools.ui.home.topbar.HomeTopBar
import com.mty.exptools.ui.theme.ExptoolsTheme

@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    var currentRoute by rememberSaveable { mutableStateOf(HomeDestination.List.route) }

    Scaffold(
        topBar = { HomeTopBar("ExpTools") },
        bottomBar = {
            HomeBottomBar(
                currentRoute = currentRoute,
                onItemClick = {
                    currentRoute = it
                    navController.navigate(it)
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.List.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeDestination.List.route) { ListScreen() }
            composable(HomeDestination.More.route) { MoreScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenContentPreview() {
    ExptoolsTheme {
        HomeScreen()
    }
}
