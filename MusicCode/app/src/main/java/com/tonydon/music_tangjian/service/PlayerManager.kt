package com.tonydon.music_tangjian.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

object PlayerManager {
    lateinit var binder: MusicService.MusicBinder
    var isBind = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as MusicService.MusicBinder
            isBind = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBind = false
        }
    }

    fun init(context: Context) {
        if (isBind) return
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
    }

}