package com.xujiaji.toolworkmanager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWorker(val context: Context, userParameters: WorkerParameters) : Worker(context, userParameters) {

    override fun doWork(): Result {
        try {

            val uploadTitle = inputData.getString(MainActivity.KEY_UPLOAD_TITLE) ?: return Result.failure()
            val uploadDesc = inputData.getString(MainActivity.KEY_UPLOAD_DESC) ?: return Result.failure()


            for (i in 0..3) {
                Thread.sleep(1000)

                Log.i("furkanpasa", "正在上传图片 $i")
            }

            NotificationManagerCompat.from(context).sendNotification(context, uploadTitle, uploadDesc)

            return Result.success()
        }catch (e: Exception) {
            return Result.failure()
        }
    }
}