package com.example.soar.AlarmPage

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.soar.MainActivity
import com.example.soar.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.jvm.java

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // token을 서버로 전송
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // 1. notification-only 메시지도 data 형태로 변환
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "No title"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "No body"

        Log.d("FCM", "Title: $title")
        Log.d("FCM", "Body: $body")

        //클릭 시 이동할 Activity 설정
        val intent = Intent(this, MainActivity::class.java).apply{
            putExtra("openFragment", "archivingFragment")
        }

        // TaskStackBuilder를 사용해 Activity의 back stack을 생성
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 2. 알림 생성
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_ID
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            NotificationCompat.Builder(applicationContext, channelId)
        } else {
            NotificationCompat.Builder(applicationContext)
        }

        builder.setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo_small)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "default_channel_id"
        const val CHANNEL_NAME = "Default Channel"
    }
}
