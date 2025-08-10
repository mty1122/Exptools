package com.mty.exptools.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    // 记录导航次数
    var navCount by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            HomeTopBar("ExpTools"){
                navCount++ // 单击搜索按钮时导航次数+1
            }
        },
        bottomBar = {
            HomeBottomBar(
                currentRoute = currentRoute,
                onItemClick = { targetRoute->
                    if (targetRoute != currentRoute) {
                        currentRoute = targetRoute
                        navController.navigate(targetRoute)
                        navCount++ // 跳转至其他页面时导航次数+1
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.List.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeDestination.List.route) { ListScreen(navCount = navCount) }
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
