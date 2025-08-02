package com.tonydon.music_tangjian.utils

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.data.MusicInfo

object ConfigUtils {
    private val mmkv = MMKV.defaultMMKV()
    private val gson = Gson()

    fun isAgreed(): Boolean = mmkv.decodeBool("key_privacy_agreed", false)

    fun setAgreed(agreed: Boolean) {
        mmkv.encode("key_privacy_agreed", agreed)
    }

    fun isFavorite(music: MusicInfo): Boolean {
        val set = mmkv.getStringSet("favorite_music_ids", emptySet()) ?: emptySet()
        return music.id.toString() in set
    }

    fun setFavorite(music: MusicInfo, fav: Boolean) {
        val set = mmkv.getStringSet("favorite_music_ids", emptySet())!!.toMutableSet()
        if (fav) {
            set.add(music.id.toString())
            mmkv.putString("favorite_music_detail_${music.id}", gson.toJson(music)) // 保存详情信息
        } else {
            set.remove(music.id.toString())
            mmkv.removeValueForKey("favorite_music_detail_${music.id}")
        }
        mmkv.putStringSet("favorite_music_ids", set)
    }

    fun getAllFavoriteMusic(): List<MusicInfo> {
        val mmkv = MMKV.defaultMMKV()
        val set = mmkv.getStringSet("favorite_music_ids", emptySet()) ?: return emptyList()
        return set.mapNotNull { id ->
            mmkv.getString("favorite_music_detail_$id", null)
        }.mapNotNull { jsonStr ->
            runCatching { gson.fromJson(jsonStr, MusicInfo::class.java) }.getOrNull()
        }
    }

}
