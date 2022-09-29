package com.mungaicodes.workmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mungaicodes.workmanager.ui.theme.FileApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class DownloadWorker(private val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        startForeGroundService()
        delay(5000)
        val response = FileApi.instance.downloadImage()
        //.let {} means if bondy != null
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { stream ->
                    try {
                        stream.write(body.bytes())
                    } catch (e: IOException) {
                        return@withContext Result.failure(
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage
                            )
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toURI().toString()
                    )
                )
            }
        }
        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network Error"
                )
            )
        }
        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Unknown Error"
            )
        )
    }

    private suspend fun startForeGroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, "download_channel")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Downloading")
                    .setContentTitle("Download in progress")
                    .build()
            )
        )
    }
}