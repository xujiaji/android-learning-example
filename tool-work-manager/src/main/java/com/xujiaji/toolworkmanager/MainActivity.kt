package com.xujiaji.toolworkmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.xujiaji.toolworkmanager.EmotionAnalysisWorker.Companion.KEY_USER_EMOTION_RESULT
import com.xujiaji.toolworkmanager.databinding.ActivityMainBinding
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        val KEY_USER_COMMENT_TEXT = "key.user.comment.text"
        val KEY_DOWNLOAD_TITLE = "key.download.title"
        val KEY_DOWNLOAD_DESC = "key.download.desc"
        val KEY_UPLOAD_TITLE = "key.upload.title"
        val KEY_UPLOAD_DESC = "key.upload.desc"
        val TAG_SEND_LOG = "tag.send.log"
        val CHANNEL_ID = "4747"
    }

    private lateinit var binding: ActivityMainBinding

    private fun createNotificationChannel() {
        val channel =
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("渠道名")
                .setDescription("渠道描述")
                .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        binding.btnTest.setOnClickListener {
            testSendOneTimeRequest()
        }

        binding.btnTest2.setOnClickListener {
            testSendImageHandler()
        }

        binding.btnTest3.setOnClickListener {
            testPeriodically()
        }

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                WorkManager.getInstance(this).cancelAllWorkByTag(TAG_SEND_LOG)
            }
        }

        binding.btnTest4.setOnClickListener {
            testQueryCancelAndFailWork()
        }
    }

    private val mMainThreadExecutor = Executor { command ->
        runOnUiThread { command.run() }
    }

    private fun testQueryCancelAndFailWork() {
        val workQuery = WorkQuery.Builder
            .fromTags(listOf(TAG_SEND_LOG))
            .addStates(listOf(WorkInfo.State.FAILED, WorkInfo.State.CANCELLED))
            .build()
        val workInfosListenableFuture = WorkManager.getInstance(this).getWorkInfos(workQuery)
        workInfosListenableFuture.addListener(
            {
                for (workInfo in workInfosListenableFuture.get()) {
                    Toast.makeText(this, workInfo.state.name, Toast.LENGTH_SHORT).show()
                }
            },
            mMainThreadExecutor
        )
    }

    private fun testSendOneTimeRequest() {
        val workManager = WorkManager.getInstance(this)

        val data = Data.Builder()
            .putString(KEY_USER_COMMENT_TEXT, "hello word")
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val emotionAnalysisWorker = OneTimeWorkRequestBuilder<EmotionAnalysisWorker2>()
            // 配置优先级
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(data)
            .setConstraints(constraints)
            .build()

//        workManager.enqueue(emotionAnalysisWorker)
        workManager.enqueueUniqueWork(
            "test-work-name",
            ExistingWorkPolicy.REPLACE,
            emotionAnalysisWorker
        )
//
//        workManager.getWorkInfoByIdLiveData(emotionAnalysisWorker.id)
//            .observe(this) { workInfo ->
//                if (workInfo == null) {
//                    Toast.makeText(this, "WorkInfo is null", Toast.LENGTH_SHORT).show()
//                    return@observe
//                }
//                binding.textViewWorkState.text = workInfo.state.name
//                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
//                    val userEmotionResult = workInfo.outputData.getString(KEY_USER_EMOTION_RESULT)
//                    Toast.makeText(this, userEmotionResult, Toast.LENGTH_SHORT).show()
//                }
//            }

        workManager.getWorkInfosForUniqueWorkLiveData("test-work-name")
            .observe(this) { workInfos ->
                for (i in 0 until workInfos.size) {
                    val workInfo = workInfos[i]
                    binding.textViewWorkState.text = workInfo.state.name
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val userEmotionResult =
                            workInfo.outputData.getString(KEY_USER_EMOTION_RESULT)
                        Toast.makeText(this, userEmotionResult, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun testSendImageHandler() {

        val workManager = WorkManager.getInstance(this)

        val downloadData = workDataOf(
            KEY_DOWNLOAD_TITLE to "下载器",
            KEY_DOWNLOAD_DESC to "图片下载成功"
        )

        val uploadData = workDataOf(
            KEY_UPLOAD_TITLE to "上传器",
            KEY_UPLOAD_DESC to "图片上传成功"
        )

        val constraintUpload = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()

        val constraintDownload = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val compressImage = OneTimeWorkRequestBuilder<CompressWorker>()
            .build()

        val filterImage = OneTimeWorkRequestBuilder<FilterWorker>()
            .build()

        val uploadImage = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraintUpload)
            .setInputData(uploadData)
            .build()

        val downloadImage = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraintDownload)
            .setInputData(downloadData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        val parallelWork = mutableListOf<OneTimeWorkRequest>()
        parallelWork.add(compressImage)
        parallelWork.add(filterImage)

        workManager
            .beginWith(parallelWork)
            .then(uploadImage)
            .then(downloadImage)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(downloadImage.id)
            .observe(this) { workInfoDownload ->
                val progress = workInfoDownload.progress
                val value = progress.getInt(DownloadWorker.PROGRESS, 0)
                binding.textViewWorkState.text = "${workInfoDownload.state.name}  $value"
            }

    }

    private fun testPeriodically() {
        val workManager = WorkManager.getInstance(this)

        val sendingLog = PeriodicWorkRequestBuilder<SendLogWorker>(15, TimeUnit.SECONDS)
            .addTag(TAG_SEND_LOG)
            .build()

        workManager.enqueue(sendingLog)

        binding.switch1.isChecked = true
        workManager.getWorkInfoByIdLiveData(sendingLog.id)
            .observe(this) { workInfo ->
                binding.textViewWorkState.text = workInfo.state.name
            }
    }


}