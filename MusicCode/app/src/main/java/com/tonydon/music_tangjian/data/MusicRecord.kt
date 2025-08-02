package com.tonydon.music_tangjian.data

/*{
    "moduleConfigId": 1,
    "moduleName": "banner",
    "style": 1,
    "musicInfoList": [
    {
        "id": 2,
        "musicName": "勇敢的人先享受世界",
        "author": "文韬",
        "coverUrl": "http://p2.music.126.net/k1yZYmlQN1PwEHcNDebrIA==/109951169490500312.jpg",
        "musicUrl": "https://cdn.cnbj1.fds.api.mi-img.com/migame-monitor-public/music/video/2.mp3",
        "lyricUrl": "https://cdn.cnbj1.fds.api.mi-img.com/migame-monitor-public/music/lrc/2.txt"
    }
    ]
}*/
data class MusicRecord(
    val moduleConfigId: Int,
    val moduleName: String,
    val style: Int,
    val musicInfoList: List<MusicInfo>
)