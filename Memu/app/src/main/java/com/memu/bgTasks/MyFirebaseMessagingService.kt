package com.memu.bgTasks

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iapps.logs.com.pascalabs.util.log.activity.ActivityPascaLog
import com.iapps.logs.com.pascalabs.util.log.helper.Helper
import com.memu.ActivityMain
import com.memu.R
import com.memu.etc.Keys

import java.io.IOException
import java.net.URL

import io.paperdb.Paper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        sendNotification(notification!!, data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        println("Notification_received Notification_received " + data + " " + notification.clickAction)

        val icon = BitmapFactory.decodeResource(resources, R.drawable.memu_logo)

        val intent = Intent(this, ActivityMain::class.java)
        intent.putExtra("message", notification.body)
        intent.putExtra("title", notification.title)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setContentInfo(notification.title)
            .setLargeIcon(icon)
            .setColor(Color.RED)
            .setLights(Color.RED, 1000, 300)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.memu_logo)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Channel is required for Android O and above
        createChannel(notificationManager)

        notificationManager.notify(0, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(notificationManager : NotificationManager): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "channel description"
            channel.setShowBadge(true)
            channel.canShowBadge()
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
            notificationManager.createNotificationChannel(channel)

        }
        return "memu notification"

    }

    private fun startForegroundService(content : RemoteMessage) {
        val contentView = RemoteViews(packageName, R.layout.custom_notification_layout)
        var channel: String = ""

        val bundle = content.data
        val message  = bundle.get(Keys.MESSAGE)
        val intent = Intent(this, ActivityMain::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val mBuilder = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.memu_logo)
            .setContentText(bundle.get(Keys.MESSAGE))
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE) //Important for heads-up notification
            .setPriority(Notification.PRIORITY_HIGH)
            .setContent(contentView)
            .setContentIntent(pendingIntent)

        contentView.setTextViewText(R.id.text, message);
        val buildNotification = mBuilder.build()
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26)
            channel = createChannel(mNotifyMgr)
        else {
            channel = ""
        }
        mNotifyMgr.notify(1, buildNotification)
    }
}