package com.xujiaji.toolworkmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class SendLogWorker(context: Context, userParameters: WorkerParameters) : Worker(context, userParameters) {

    override fun doWork(): Result {
        try {
            for (i in 0..3) {
                Thread.sleep(1000)
                Log.i("furkanpasa", "Sending log $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}