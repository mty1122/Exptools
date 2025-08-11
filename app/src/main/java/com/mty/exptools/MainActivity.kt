package com.mty.exptools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mty.exptools.ui.BaseScreen
import com.mty.exptools.ui.home.HomeScreen
import com.mty.exptools.ui.theme.ExptoolsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExptoolsTheme {
                BaseScreen()
            }
        }
    }
}