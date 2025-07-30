package com.tonydon.music_tangjian

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.utils.CacheManager

class App : Application() {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(applicationContext)   // 初始化 mmkv
        CacheManager.init(applicationContext)   // 初始化缓存
    }
}