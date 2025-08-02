package com.tonydon.music_tangjian

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.service.PlayerManager
import com.tonydon.music_tangjian.utils.CacheManager
import com.tonydon.music_tangjian.utils.MessageCryptoUtil

class App : Application() {
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(applicationContext)   // 初始化 mmkv
        CacheManager.init(applicationContext)   // 初始化缓存
        PlayerManager.init(this) // 初始化播放服务
        MessageCryptoUtil.generateAESKeyIfNecessary()   // 初始化密钥
    }
}