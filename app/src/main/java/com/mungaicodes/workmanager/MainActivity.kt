package com.mungaicodes.workmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter
import com.mungaicodes.workmanager.ui.theme.WorkManagerTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downLoadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>()
            .build()

        val workManager = WorkManager.getInstance(applicationContext)
//            .setBackoffCriteria(
//                BackoffPolicy.LINEAR, 30, TimeUnit.MICROSECONDS
//            )

        setContent {
            WorkManagerTheme {
                val workInfos = workManager
                    .getWorkInfosForUniqueWorkLiveData("download")
                    .observeAsState()
                    .value
                val downloadInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == downLoadRequest.id }
                }

                val filterInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == colorFilterRequest.id }
                }

                val imageUri by derivedStateOf {
                    val downloadUri =
                        downloadInfo?.outputData?.getString(WorkerKeys.IMAGE_URI)
                            ?.toUri()
                    val filterUri =
                        downloadInfo?.outputData?.getString(WorkerKeys.FILTER_URI)
                            ?.toUri()
                    filterUri ?: downloadUri
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberImagePainter(data = uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Button(
                        onClick = {
                            workManager.beginUniqueWork(
                                "download",
                                ExistingWorkPolicy.KEEP,
                                downLoadRequest
                            ).then(colorFilterRequest).enqueue()
                        },
                        enabled = downloadInfo?.state != WorkInfo.State.RUNNING
                    ) {
                        Text(text = "Start Download")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (downloadInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Downloading....")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Download Succeeded")
                        WorkInfo.State.FAILED -> Text(text = "Download Failed")
                        WorkInfo.State.CANCELLED -> Text(text = "Download Cancelled")
                        WorkInfo.State.ENQUEUED -> Text(text = "Download Enqueued")
                        else -> Text(text = "Download Blocked")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (filterInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Applying filter....")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Filter Succeeded")
                        WorkInfo.State.FAILED -> Text(text = "FilterFailed")
                        WorkInfo.State.CANCELLED -> Text(text = "Filter Cancelled")
                        WorkInfo.State.ENQUEUED -> Text(text = "Filter Enqueued")
                        else -> Text(text = "Filter Blocked")
                    }

                }
            }
        }
    }
}

