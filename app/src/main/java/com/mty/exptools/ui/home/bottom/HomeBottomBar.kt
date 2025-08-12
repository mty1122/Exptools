package com.mty.exptools.ui.home.bottom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mty.exptools.ui.home.HomeDestination
import com.mty.exptools.ui.theme.ExptoolsTheme

@Composable
fun HomeBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == HomeDestination.List.route,
            onClick = { onItemClick(HomeDestination.List.route) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "待办") },
            label = { Text("待办") }
        )
        NavigationBarItem(
            selected = currentRoute == HomeDestination.More.route,
            onClick = { onItemClick(HomeDestination.More.route) },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "更多") },
            label = { Text("更多") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBottomBarPreview() {
    ExptoolsTheme {
        HomeBottomBar(HomeDestination.List.route) { }
    }
}
