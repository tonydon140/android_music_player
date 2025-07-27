package com.tonydon.music_tangjian.utils

import com.tencent.mmkv.MMKV

object ConfigUtils {
    private val mmkv = MMKV.defaultMMKV()

    fun isAgreed(): Boolean = mmkv.decodeBool("key_privacy_agreed", false)

    fun setAgreed(agreed: Boolean) {
        mmkv.encode("key_privacy_agreed", agreed)
    }
}
