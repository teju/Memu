package com.memu.bgTasks

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.memu.ui.activity.ActivityMain
import com.memu.R
import com.memu.etc.Keys

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("Notification_received Notification_received " + remoteMessage.data)

        val notification = remoteMessage.notification
        val data = remoteMessage.data
        sendNotification(notification!!, data)
        startForegroundService(remoteMessage)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {

        val icon = BitmapFactory.decodeResource(resources, R.drawable.memu_logo)

        val intent = Intent(this, ActivityMain::class.java)
        intent.putExtra("message", notification.body)
        intent.putExtra("title", notification.title)
        intent.putExtra("body", data.get("body"))

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
        createChannel(this)

        notificationManager.notify(0, notificationBuilder.build())
    }

    @NonNull
    @TargetApi(26)
    @Synchronized
    private fun createChannel(context: Context): String {
        if (Build.VERSION.SDK_INT >= 26){

            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = "memu notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel("memu notification", name, importance)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.BLUE
            mNotificationManager?.createNotificationChannel(mChannel)
        }
        return "memu notification"

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService(content : RemoteMessage) {
        val contentView = RemoteViews(packageName, R.layout.custom_notification_layout)
        val channel: String
        if (Build.VERSION.SDK_INT >= 26)
            channel = createChannel(this)
        else {
            channel = ""
        }
        val bundle = content.data
        val message  = bundle.get(Keys.MESSAGE)
        //val intent = Intent(this, ActivityMain::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0 /* Request code */, intent,
//            PendingIntent.FLAG_ONE_SHOT
//        )
        val mBuilder = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.memu_logo)
            .setContentText(bundle.get(Keys.MESSAGE))
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE) //Important for heads-up notification
            .setPriority(Notification.PRIORITY_HIGH)
            .setContent(contentView)
            .setAutoCancel(true)
            //.setContentIntent(pendingIntent)
        val broadcast =  Intent();
        broadcast.putExtra("message", message)
        broadcast.putExtra("title", content.notification?.title)
        broadcast.putExtra("body", content.data.get("body"))
        broadcast.setAction("OPEN_NEW_ACTIVITY");
        sendBroadcast(broadcast);
        contentView.setTextViewText(R.id.text, message);
        val buildNotification = mBuilder.build()
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //mNotifyMgr.notify(1, buildNotification)
    }

}
