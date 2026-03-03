package com.mty.exptools.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.mty.exptools.ui.home.center.FrostedBottomBand
import com.mty.exptools.ui.home.center.list.ListScreen
import com.mty.exptools.ui.home.center.more.MoreScreen
import com.mty.exptools.ui.home.topbar.HomeTopBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    topNavController: NavHostController
) {

    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var currentRoute = navBackStackEntry?.destination?.route

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
        if (!isOnHome && searchExpanded && query.isBlank()) {
            searchExpanded = false
            query = ""
        }
    }

    // 实现单击状态栏滚动至顶部
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val moreState = rememberLazyListState()
    val currentListState = if (currentRoute == HomeDestination.List.route) listState else moreState
    // 用于记录上次点击的时间
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val onTopBarDoubleClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 300) { // 300毫秒内双击有效
            scope.launch {
                // 滚动到第一项
                currentListState.animateScrollToItem(0)
            }
        }
        lastClickTime = currentTime
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            HomeTopBar(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // 移除水波纹，避免干扰搜索栏等组件
                ) {
                    onTopBarDoubleClick()
                },
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
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
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
                FrostedBottomBand(
                    mainState = listState,
                    bandHeight = innerPadding.calculateBottomPadding(),
                    blurRadius = 18.dp,
                    overlay = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.28f)
                ) { listState, isMain->
                    ListScreen(
                        navCount = navCount,
                        query = query,
                        lazyListState = listState,
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        isMain = isMain,
                        topNavController = topNavController
                    )
                }
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
            ) {
                FrostedBottomBand(
                    mainState = moreState,
                    bandHeight = innerPadding.calculateBottomPadding(),
                    blurRadius = 10.dp,
                    overlay = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.8f)
                ) { listState, _->
                    MoreScreen(
                        setBackgroundBlur = ::setBackgroundBlur,
                        lazyListState = listState,
                        navController = navController,
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
            }
        }
    }

}
