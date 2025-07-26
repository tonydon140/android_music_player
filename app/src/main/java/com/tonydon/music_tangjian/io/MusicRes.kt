package com.tonydon.music_tangjian.io

data class MusicRes(
    val code: Int,
    val msg: String,
    val data: MusicData
)

data class MusicData(
    val records: List<MusicRecord>
)
