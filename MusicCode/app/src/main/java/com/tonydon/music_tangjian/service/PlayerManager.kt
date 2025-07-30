package com.tonydon.music_tangjian.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat

object PlayerManager {
    lateinit var binder: MusicService.MusicBinder
    var isBind = false
    private var onBinderReadyListener: OnBinderReadyListener? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as MusicService.MusicBinder
            isBind = true
            onBinderReadyListener?.onReady()    // 通过监听器通知服务绑定成功
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBind = false
        }
    }

    fun init(context: Context, listener: OnBinderReadyListener) {
        if (isBind) {
            return
        }
        onBinderReadyListener = listener
        val intent = Intent(context, MusicService::class.java)
        // bindService 是异步操作
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        ContextCompat.startForegroundService(context, intent)
    }

}