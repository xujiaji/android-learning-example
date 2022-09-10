package com.xujiaji.toolworkmanager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xujiaji.toolworkmanager.MainActivity.Companion.KEY_DOWNLOAD_DESC
import com.xujiaji.toolworkmanager.MainActivity.Companion.KEY_DOWNLOAD_TITLE

class DownloadWorker(val context: Context, userParameters: WorkerParameters) :
    Worker(context, userParameters) {

    companion object {
        const val PROGRESS = "progress"
    }

    override fun doWork(): Result {
        try {

            val downloadTitle = inputData.getString(KEY_DOWNLOAD_TITLE) ?: return Result.failure()
            val downloadDesc = inputData.getString(KEY_DOWNLOAD_DESC) ?: return Result.failure()

            for (i in 0..3) {
                Thread.sleep(1000)
                setProgressAsync(Data.Builder().putInt(PROGRESS, i).build())
                Log.i("furkanpasa", "正在下载图片 $i")
            }

            NotificationManagerCompat.from(context).sendNotification(context, downloadTitle, downloadDesc)

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}