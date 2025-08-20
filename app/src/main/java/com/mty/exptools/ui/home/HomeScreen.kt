package com.mty.exptools.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mty.exptools.ui.HomeRoute
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

    var backgroundBlur by rememberSaveable { mutableStateOf(false) }
    fun setBackgroundBlur(blur: Boolean) { backgroundBlur = blur }
    val blur by animateDpAsState(
        targetValue = if (backgroundBlur) 12.dp else 0.dp,
        animationSpec = tween(200),
        label = "list-blur"
    )

    // TopBar搜索功能
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    // 切换页面关闭搜索栏
    val topBackStack by topNavController.currentBackStackEntryAsState()
    val isOnHome = topBackStack?.destination?.route == HomeRoute::class.qualifiedName
    LaunchedEffect(isOnHome) {
        if (!isOnHome) {
            searchExpanded = false
            query = ""
        }
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            HomeTopBar(
                showSearchIcon = currentRoute == HomeDestination.List.route,
                searchExpanded = searchExpanded,
                query = query,
                onExpandChange = { searchExpanded = it },
                onQueryChange = { query = it }
            )
        },
        bottomBar = {
            HomeBottomBar(
                currentRoute = currentRoute,
                onItemClick = { targetRoute->
                    if (targetRoute != currentRoute) {
                        if (targetRoute != HomeDestination.List.route) { // 切换页面关闭搜索栏
                            searchExpanded = false
                            query = ""
                        }
                        currentRoute = targetRoute
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
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
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it }
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { -it }
                    )
                }
            ) {
                ListScreen(
                    navCount = navCount,
                    query = query,
                    topNavController = topNavController
                )
            }
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
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        initialOffsetX = { it }
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { it }
                    )
                }
            ) { MoreScreen(::setBackgroundBlur) }
        }
    }

}
