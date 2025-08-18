package com.mty.exptools.ui

import android.content.Context
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
import com.mty.exptools.domain.StepTimer
import com.mty.exptools.ui.home.HomeScreen
import com.mty.exptools.ui.share.edit.photo.PhotoEditScreen
import com.mty.exptools.ui.share.edit.syn.SynthesisEditScreen
import com.mty.exptools.ui.share.edit.test.TestEditScreen

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
                navController = topNavController,
                onSetAlarmForCurrent = { message, timer ->
                    setSetAlarmForCurrent(context, message, timer)
                }
            )
        }

        composable<PhotoEditRoute>(
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
            PhotoEditScreen(
                navController = topNavController,
                onSetAlarmForCurrent = { message, timer ->
                    setSetAlarmForCurrent(context, message, timer)
                }
            )
        }

        composable<TestEditRoute>(
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
            TestEditScreen(
                navController = topNavController
            )
        }
    }

}

private fun setSetAlarmForCurrent(context: Context, message: String, timer: StepTimer) {
    val minutes = (timer.remaining().coerceAtLeast(0L) + 59_999L) / 60_000L
    val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes.toInt()) }
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
        putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
    }
    context.startActivity(intent)
}