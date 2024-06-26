package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var exampleService: ExampleService? = null
    private var isServiceBound = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())

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
}