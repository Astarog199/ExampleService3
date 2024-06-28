package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var exampleService: ExampleService? = null
    private var isServiceBound = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())


    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.isNotEmpty() && permissions.values.all { it }) {

        } else {
            Toast.makeText(this, "Need permission.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Объект ServiceConnection, который используется для управления
     * связями между компонентами и сервисами.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ExampleService.localBinder
            exampleService = binder.getService()
            isServiceBound = true
            if (exampleService!!.isRunning) {
                startPolling()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        startService()


        binding.button.setOnClickListener {
            if (exampleService?.isRunning == true) {
                binding.textView.text = exampleService?.restartCountdown().toString()
            } else {
                binding.textView.text = exampleService?.startCountdown().toString()
                startPolling()
            }
        }
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                val counter = exampleService?.getValue()
                if (counter!! > 0) {
                    binding.textView.text = counter.toString()
                    handler.postDelayed(this, 1000)
                } else {
                    binding.textView.text = 0.toString()
                }
            }
        })
    }

    /**
     *  метод startService запускает и привязвает сервис к текущему компоненту.
     *    serviceIntent - указывает на сервис, который нужно привязать.
     *    connection - используется для отслеживания состояния связи с сервисом.
     *    - Context.BIND_AUTO_CREATE - если сервис еще не запущен,
     *    он должен быть автоматически создан.
     */
    private fun startService() {
        val serviceIntent = Intent(this, ExampleService::class.java)
        this.startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            Toast.makeText(this, "permission is Granted", Toast.LENGTH_SHORT).show()
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }

    }

    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(android.Manifest.permission.FOREGROUND_SERVICE)
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
    }

}