package com.tonydon.music_tangjian

import android.app.Application
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.service.PlayerManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)   // 初始化 mmkv
    }
}