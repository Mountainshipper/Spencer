package at.interactivecuriosity.imagedownload

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.interactivecuriosity.imagedownload.R
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var downloadButton: Button
    private lateinit var deleteButton: Button
    private val imageUrl = "https://www.markusmaurer.at/fhj/eyecatcher.jpg"
    private val fileName = "downloadedImage.jpg"
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        downloadButton = findViewById(R.id.downloadButton)
        deleteButton = findViewById(R.id.deleteButton)

        setupBroadcastReceiver()

        downloadButton.setOnClickListener { downloadImage(imageUrl, fileName) }

        deleteButton.setOnClickListener { deleteImage(fileName) }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun setupBroadcastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    DownloadService.ACTION_DOWNLOAD_COMPLETE -> handleDownloadComplete(intent)
                    DownloadService.ACTION_DOWNLOAD_FAILED -> handleDownloadFailed()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(DownloadService.ACTION_DOWNLOAD_COMPLETE)
            addAction(DownloadService.ACTION_DOWNLOAD_FAILED)
        }

        registerReceiver(receiver, filter)
    }

    private fun downloadImage(url: String, fileName: String) {
        showToast("Bild wird heruntergeladen")
        startDownloadService(url, fileName)
    }

    private fun deleteImage(fileName: String) {
        val file = File(getExternalFilesDir(null), fileName)
        if (file.exists()) {
            file.delete()
            runOnUiThread {
                imageView.setImageBitmap(null)
                showToast("Bild gelöscht")
            }
        }
    }
//
    private fun handleDownloadComplete(intent: Intent) {
        val filePath = intent.getStringExtra(DownloadService.KEY_BITMAP_FILE_PATH)
        if (!filePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            imageView.setImageBitmap(bitmap)
        } else {
            showToast("Fehler beim Herunterladen")
        }
        showToast("Bild heruntergeladen")
    }

    private fun handleDownloadFailed() {
        showToast("Fehler beim Herunterladen")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun startDownloadService(url: String, fileName: String) {
        val intent = Intent(this, DownloadService::class.java).apply {
            putExtra(DownloadService.KEY_IMAGE_URL, url)
            putExtra(DownloadService.KEY_FILE_NAME, fileName)
        }
        startService(intent)
    }
}
