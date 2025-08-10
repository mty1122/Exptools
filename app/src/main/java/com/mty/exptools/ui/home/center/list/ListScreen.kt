package com.mty.exptools.ui.home.center.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mty.exptools.ui.home.center.list.item.ItemOther
import com.mty.exptools.ui.home.center.list.item.ItemOtherUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotoUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotocatalysis
import com.mty.exptools.ui.home.center.list.item.ItemSynUiState
import com.mty.exptools.ui.home.center.list.item.ItemSynthesis
import com.mty.exptools.ui.home.center.list.item.ItemTest
import com.mty.exptools.ui.home.center.list.item.ItemTestUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navCount: Int) {
    val viewModel: ListViewModel = hiltViewModel()
    val itemUiStateList by viewModel.itemUiStateList.collectAsStateWithLifecycle()

    val refreshState = rememberPullToRefreshState()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    var expanded by rememberSaveable { mutableStateOf(false) } // ← hoist 展开状态
    val blur by animateDpAsState(
        targetValue = if (expanded) 12.dp else 0.dp, // 背景模糊半径
        animationSpec = tween(200),
        label = "list-blur"
    )

    // 监听导航次数变化, 以便跳转到其他界面时折叠按钮
    LaunchedEffect(navCount) {
        if (expanded) expanded = false
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            viewModel.refresh()
        },
        state = refreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = refreshState,
                isRefreshing = refreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .blur(blur),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemUiStateList, key = { it.listItemId }) { uiState ->
                when (uiState) {
                    is ItemPhotoUiState -> ItemPhotocatalysis(uiState)
                    is ItemSynUiState -> ItemSynthesis(uiState)
                    is ItemTestUiState -> ItemTest(uiState)
                    is ItemOtherUiState -> ItemOther(uiState)
                }
            }
        }

        AddItemSpeedDial(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onAdd = {},
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
