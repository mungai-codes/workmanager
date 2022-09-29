package com.mungaicodes.workmanager

import android.content.Context
import android.graphics.*
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ColorFilterWorker(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val imageFile = params.inputData.getString(WorkerKeys.IMAGE_URI)
            ?.toUri()
            ?.toFile()
        delay(5000)
        return imageFile?.let { file ->
            val bmp = BitmapFactory.decodeFile(file.absolutePath)
            val resultBmp = bmp.copy(bmp.config, true)
            val paint = Paint()
            paint.colorFilter = LightingColorFilter(0x08ff04, 1)
            val canvas = Canvas(resultBmp)
            canvas.drawBitmap(resultBmp, 0f, 0f, paint)

            withContext(Dispatchers.IO) {
                val resultImageFile = File(context.cacheDir, "cnew-image.jpg")
                val outputStream = FileOutputStream(resultImageFile)
                val successful = resultBmp.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    outputStream
                )
                if (successful) {
                    Result.success(
                        workDataOf(
                            WorkerKeys.FILTER_URI to resultImageFile.toURI().toString()
                        )
                    )
                } else Result.failure()

            }
        } ?: Result.failure()
    }
}