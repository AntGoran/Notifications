package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var downloadStatus: String
    private lateinit var selectedUri: URL
    private val NOTIFICATION_ID = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        createNotificationChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {

            if (this::selectedUri.isInitialized) {

                val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager

                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()

                val networkCallback = object  : ConnectivityManager.NetworkCallback() {

                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        custom_button.buttonState = ButtonState.Loading
                        download()
                        connectivityManager.unregisterNetworkCallback(this)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        Toast.makeText(applicationContext, getString(R.string.connection_failed_toast), Toast.LENGTH_SHORT).show()
                    }
                }
                connectivityManager.requestNetwork(networkRequest, networkCallback)
            } else { Toast.makeText(this, getString(R.string.select_option_toast), Toast.LENGTH_SHORT).show() }
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            selectedUri = when (i) {
                R.id.radioButton_Glide -> URL.GLIDE
                R.id.radioButton_Retrofit -> URL.RETROFIT
                else -> URL.UDACITY
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    val manager =
                        context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                // send success notification
                                downloadStatus = "Success"
                                custom_button.buttonState = ButtonState.Completed
                                createNotification()
                            } else {
                                // send failed notification
                                downloadStatus = "Fail"
                                custom_button.buttonState = ButtonState.Completed
                                createNotification()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedUri.uri))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    private fun createNotification() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        // Create Intent
        val contentIntent = Intent(this, DetailActivity::class.java)
        contentIntent.putExtra("filename", selectedUri.title)
        contentIntent.putExtra("status", downloadStatus)

        // Create PendingIntent
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            pendingIntent
        )

        // Get an instance of NotificationCompat.Builder
        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )

            // Set title and text
            .setContentTitle(selectedUri.title)
            .setContentText(selectedUri.text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .addAction(action)
            .setAutoCancel(true)

        // Call notify
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // Create a channel and customize behaviour
    private fun createNotificationChannel() {
        // Make version check (available  from 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, "loadingApp_notificationChannel",
                // Change importance
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download complete"

            // Get instance of NotificationManager
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private enum class URL(val uri: String, val title: String, val text: String) {
            GLIDE(
                "https://github.com/bumptech/glide.zip",
                "Glide: Image Loading Library by BumpTech",
                "Glide repository is downloaded"
            ),
            UDACITY(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter.zip",
                "Udacity: Android programming project starter",
                "Udacity starter project is downloaded"
            ),
            RETROFIT(
                "https://github.com/square/retrofit.zip",
                "Retrofit: Type-safe HTTP client by Square, Inc ",
                "Retrofit repository is downloaded"
            )
        }
        private const val CHANNEL_ID = "channelId"
    }

}
