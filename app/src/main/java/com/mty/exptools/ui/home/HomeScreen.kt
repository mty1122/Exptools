package com.mty.exptools.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mty.exptools.ui.home.bottom.HomeBottomBar
import com.mty.exptools.ui.home.center.list.ListScreen
import com.mty.exptools.ui.home.center.more.MoreScreen
import com.mty.exptools.ui.home.topbar.HomeTopBar

@Composable
fun HomeScreen(
    topNavController: NavHostController
) {

    val navController = rememberNavController()
    var currentRoute by rememberSaveable { mutableStateOf(HomeDestination.List.route) }

    // 记录导航次数
    var navCount by remember { mutableIntStateOf(0) }
    var isList by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            HomeTopBar(
                title = "ExpTools",
                isList = isList,
                onSearchButtonClick = { navCount++ } // 单击搜索按钮时导航次数+1
            )
        },
        bottomBar = {
            HomeBottomBar(
                currentRoute = currentRoute,
                onItemClick = { targetRoute->
                    if (targetRoute != currentRoute) {
                        isList = targetRoute == HomeDestination.List.route
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
            composable(
                route = HomeDestination.List.route,
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it }
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { -it }
                    )
                },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { ListScreen(navCount = navCount, topNavController = topNavController) }
            composable(
                route = HomeDestination.More.route,
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        initialOffsetX = { it }
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { it }
                    )
                },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) { MoreScreen() }
        }
    }

}
