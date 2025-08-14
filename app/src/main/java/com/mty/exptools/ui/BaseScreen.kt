package com.mty.exptools.ui

import android.content.Intent
import android.icu.util.Calendar
import android.provider.AlarmClock
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mty.exptools.ui.home.HomeScreen
import com.mty.exptools.ui.share.edit.syn.SynthesisEditScreen

@Composable
fun BaseScreen() {

    val topNavController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = topNavController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { HomeScreen(topNavController) }

        composable<SynthesisEditRoute>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it })
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it })
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it })
            }
        ) {
            SynthesisEditScreen(
                onBack = { topNavController.popBackStack() },
                onSetAlarmForCurrent = { material, idx, step ->
                    val minutes = (step.timer.remaining().coerceAtLeast(0L) + 59_999L) / 60_000L
                    val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes.toInt()) }
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
                        putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
                        putExtra(AlarmClock.EXTRA_MESSAGE, "合成-${material} 步骤${idx+1} ${step.content}结束")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }

}