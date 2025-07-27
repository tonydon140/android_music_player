package com.tonydon.music_tangjian.io

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/*{
    "id": 1,
    "musicName": "夏日离别练习",
    "author": "蒲熠星",
    "coverUrl": "http://p1.music.126.net/Qlpe3y7sSnTUSC7_vMvyoA==/109951169718820731.jpg",
    "musicUrl": "https://cdn.cnbj1.fds.api.mi-img.com/migame-monitor-public/music/video/1.mp3",
    "lyricUrl": "https://cdn.cnbj1.fds.api.mi-img.com/migame-monitor-public/music/lrc/01.txt"
}*/
@Parcelize
data class MusicInfo(
    val id: Int,
    val musicName: String,
    val author: String,
    val coverUrl: String,
    val musicUrl: String,
    val lyricUrl: String,
) : Parcelable
