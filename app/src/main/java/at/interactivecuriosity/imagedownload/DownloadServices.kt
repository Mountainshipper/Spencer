package at.interactivecuriosity.imagedownload

import android.app.IntentService
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    override fun onHandleIntent(intent: Intent?) {
        val imageUrl = intent?.getStringExtra(KEY_IMAGE_URL) ?: return
        val fileName = intent.getStringExtra(KEY_FILE_NAME) ?: return

        Log.i(LOG_TAG, "DownloadService started with $imageUrl and $fileName")

        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val file = File(getExternalFilesDir(null), fileName)

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            val downloadCompleteIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
                putExtra(KEY_BITMAP_FILE_PATH, file.absolutePath)
            }

            sendBroadcast(downloadCompleteIntent)

        } catch (e: Exception) {
            e.printStackTrace()

            val downloadFailedIntent = Intent(ACTION_DOWNLOAD_FAILED)
            sendBroadcast(downloadFailedIntent)
        }
    }

    companion object {
        private const val LOG_TAG = "DownloadService"
        const val ACTION_DOWNLOAD_COMPLETE = "DownloadComplete"
        const val ACTION_DOWNLOAD_FAILED = "DownloadFailed"
        const val KEY_IMAGE_URL = "imageUrl"
        const val KEY_FILE_NAME = "fileName"
        const val KEY_BITMAP_FILE_PATH = "bitmapFilePath"
    }
}
