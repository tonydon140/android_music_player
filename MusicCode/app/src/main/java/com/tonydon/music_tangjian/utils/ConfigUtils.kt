package com.tonydon.music_tangjian.utils

import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.io.MusicInfo

object ConfigUtils {
    private val mmkv = MMKV.defaultMMKV()

    fun isAgreed(): Boolean = mmkv.decodeBool("key_privacy_agreed", false)

    fun setAgreed(agreed: Boolean) {
        mmkv.encode("key_privacy_agreed", agreed)
    }

    fun isFavorite(music: MusicInfo): Boolean {
        return mmkv.decodeBool("${music.id}_fav", false)
    }

    fun setFavorite(music: MusicInfo, fav: Boolean) {
        mmkv.encode("${music.id}_fav", fav)
    }
}
