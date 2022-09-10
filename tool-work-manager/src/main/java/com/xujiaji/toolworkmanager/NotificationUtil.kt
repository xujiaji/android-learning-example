package com.xujiaji.toolworkmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xujiaji.toolworkmanager.MainActivity.Companion.CHANNEL_ID

fun NotificationManagerCompat.sendNotification(context: Context, title: String = "成功", desc : String = "任务完成") {

    val NOTIFY_ID = 42633687

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(desc)
        .setSmallIcon(R.drawable.ic_baseline_celebration_24)

    notify(NOTIFY_ID, notification.build())
}