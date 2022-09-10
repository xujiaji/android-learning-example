package com.xujiaji.toolworkmanager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.xujiaji.toolworkmanager.MainActivity.Companion.KEY_USER_COMMENT_TEXT

class EmotionAnalysisWorker2(val context: Context, userParameters: WorkerParameters) :
    CoroutineWorker(context, userParameters) {

    companion object {
        val KEY_USER_EMOTION_RESULT = "key.user.emotion.result"
    }

    override suspend fun doWork(): Result {
        val text = inputData.getString(KEY_USER_COMMENT_TEXT)

        Log.i("xxxxx", Thread.currentThread().name)
        try {
            for (i in 0..3) {
                Thread.sleep(1000)
                Log.i("furkanpasa", "Emotion Analysing $i")
            }

            val outputData = Data.Builder()
                .putString(KEY_USER_EMOTION_RESULT, getUserEmotion(text))
                .build()

            return Result.success(outputData)
        } catch (e: Exception) {
            return Result.failure()
        }

    }

    fun getUserEmotion(userText : String?): String {

        val emotionList = listOf("Sad", "Happy", "Angry", "Surprise", "Tired", "Bored")

        return emotionList.random()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(42633687, NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setContentTitle("测试A")
            .setContentText("测试描述信息getForegroundInfoAsync")
            .setSmallIcon(R.drawable.ic_baseline_celebration_24)
            .build())
    }

}