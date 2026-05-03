package com.id.titikjemputmitra.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager // Pastikan ini diimport
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebase : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        if (token == null) return
        val userPref = UserPreference(applicationContext)
        if (userPref.isLoggedIn()) {
            ApiConfig.getApiService(applicationContext).updateFcmToken(token)
                .enqueue(object : Callback<GeneralResponse> {
                    override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                        Log.d(TAG, "Token FCM sinkron: ${response.body()?.status}")
                    }
                    override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                        Log.e(TAG, "Gagal sinkron token: ${t.message}")
                    }
                })
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived Mitra. Data: ${message.data}")

        // 1. Ekstrak data dari payload backend CodeIgniter
        val title = message.data["title"] ?: "Titik Jemput Mitra"
        val body  = message.data["message"] ?: ""
        val type  = message.data["type"] ?: ""

        // 2. Munculkan Notifikasi di Tray (Layar Atas)
        sendNotification(title, body)

        // 3. LOGIKA BROADCAST (Agar UI refresh otomatis tanpa ditarik)
        when (type) {
            "chat" -> {
                // Sinyal untuk InboxFragment & ChatRoomFragment
                val intent = Intent(ACTION_NEW_CHAT_MESSAGE).apply {
                    putExtra("room_id", message.data["room_id"])
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                Log.d(TAG, "Broadcast chat dikirim ke UI Mitra")
            }
            "NEW_ORDER", "UPDATE_STATUS" -> {
                // Sinyal untuk OrderFragment Mitra
                val intent = Intent(ACTION_UPDATE_STATUS).apply {
                    putExtra("type", type)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                Log.d(TAG, "Broadcast Order dikirim ke UI Mitra")
            }
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Gunakan ID unik agar notifikasi tidak saling menimpa
        val uniqueID = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, uniqueID, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(uniqueID, notificationBuilder.build())
    }

    companion object {
        private val TAG = MyFirebase::class.java.simpleName

        // Konstanta Sinyal (Harus sama dengan yang didengarkan di Fragment)
        const val ACTION_NEW_CHAT_MESSAGE = "com.id.titikjemputmitra.NEW_CHAT_MESSAGE"
        const val ACTION_UPDATE_STATUS    = "com.id.titikjemputmitra.UPDATE_STATUS"

        private const val NOTIFICATION_CHANNEL_ID = "titikjemput_mitra_channel_01"
        private const val NOTIFICATION_CHANNEL_NAME = "Mitra Notification"
    }
}