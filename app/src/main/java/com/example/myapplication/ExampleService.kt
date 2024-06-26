package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat


class ExampleService : Service() {

    private val binder = localBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val channelId = "CHANNEL_ID"
    private val notificationId = 1

    private lateinit var notificationIntent: Intent
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager

    var isRunning = false
    private var counter = 20

    inner class localBinder : Binder() {
        fun getService(): ExampleService = this@ExampleService
    }



    override fun onCreate() {
        val channel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        notificationIntent = Intent(this, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        notificationManager.deleteNotificationChannel(channelId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun getValue(): Int{
        return counter
    }

    fun restartCountdown(): Int {
        return 20
    }

     fun startCountdown(): Int {
        counter = 20
        isRunning = true
         startForegroundService()
        handler.post(object : Runnable{
            override fun run() {
                if (counter>0){
                    handler.postDelayed(this,1000)
                    counter--
                    updateForegroundServiceCounter(counter)
                }
                else{
                    isRunning = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }

        })
        return counter
    }

    private fun startForegroundService(){
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Samples")
            .setContentText("Progress: $counter")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(notificationId, notification)
    }

    private fun updateForegroundServiceCounter(counter: Int) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Samples")
            .setContentText("Progress: $counter")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notificationId, notification)
    }
}