package com.mty.exptools.ui.home.topbar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mty.exptools.ui.theme.ExptoolsTheme

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    searchExpanded: Boolean,
    query: String,
    showSearchIcon: Boolean,
    onQueryChange: (String) -> Unit,
    onExpandChange: (Boolean) -> Unit,
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = searchExpanded,
                transitionSpec = {
                    if (targetState) {
                        // 标题上去，搜索框从下上来
                        (slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically(tween(180)) { -it / 2 } + fadeOut(tween(180)))
                    } else {
                        // 搜索框下去，标题从上下来
                        (slideInVertically(tween(220)) { -it / 2 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)))
                    }
                },
                label = "title-search-vertical"
            ) { expanded ->
                if (expanded) {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        placeholder = { Text("搜索…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                } else {
                    Text("Exptools")
                }
            }
        },
        actions = {
            AnimatedContent(
                targetState = searchExpanded,
                transitionSpec = {
                    if (targetState) {
                        // 🔍 上去，× 从下上来
                        (slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically(tween(180)) { -it / 2 } + fadeOut(tween(180)))
                    } else {
                        // × 下去，🔍 从上下来
                        (slideInVertically(tween(220)) { -it / 2 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)))
                    }
                },
                label = "actions-search-vertical"
            ) { expanded ->
                if (expanded) {
                    IconButton(onClick = {
                        onQueryChange("")
                        onExpandChange(false)
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭搜索")
                    }
                } else if (showSearchIcon) {
                    IconButton(onClick = { onExpandChange(true) }) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    var searchExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    ExptoolsTheme {
        HomeTopBar(
            showSearchIcon = true,
            searchExpanded = searchExpanded,
            query = query,
            onExpandChange = { searchExpanded = it },
            onQueryChange = { query = it }
        )
    }
}