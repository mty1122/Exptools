package com.mty.exptools.ui.home.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mty.exptools.ui.theme.ExptoolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    title: String,
    searchButtonOnClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        actions = {
            IconButton(onClick = searchButtonOnClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    ExptoolsTheme {
        HomeTopBar("ExpTools"){}
    }
}