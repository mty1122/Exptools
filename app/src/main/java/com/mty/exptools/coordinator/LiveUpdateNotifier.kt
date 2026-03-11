package com.mty.exptools.coordinator

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mty.exptools.ExptoolsApp.Companion.context
import com.mty.exptools.MainActivity

data class LiveTask(
    val stableKey: String,
    val title: String,
    val content: String,
    val smallIcon: Int,
    val finishAtEpochMillis: Long,
    val remainingMillis: Long
)

object LiveUpdateNotifier {
    private const val NOTIFICATION_ID = 20001
    private const val CHANNEL_ID = "live_updates"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showOrUpdate(task: LiveTask) {
        ensureChannel()

        val notification = buildNotification(task)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(task: LiveTask): Notification {
        val pendingIntent = createLaunchAppPendingIntent()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(task.smallIcon)
            .setContentTitle(task.title)
            .setContentText(task.content)
            .setSubText("实验进行中")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(task.finishAtEpochMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setRequestPromotedOngoing(true)

        return builder.build()
    }

    private fun createLaunchAppPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_live_update", true)
        }
        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = android.app.NotificationChannel(
            CHANNEL_ID,
            "实时通知",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "实验进行中倒计时"
        }
        manager.createNotificationChannel(channel)
    }
}