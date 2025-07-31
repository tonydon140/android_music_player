package com.tonydon.music_tangjian.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import com.tonydon.music_tangjian.io.MusicInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

object PlayerManager {
    lateinit var binder: MusicService.MusicBinder
    var isBind = false
    private var currentIndex = 0

    private val playlistSet = mutableSetOf<MusicInfo>()
    private val _playlist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val playlist: StateFlow<List<MusicInfo>> = _playlist


    val currentMusic = MutableStateFlow<MusicInfo?>(null)
    val isPlaying = MutableStateFlow(false)
    val playMode = MutableStateFlow(0)
    val duration = MutableStateFlow(0L)


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
        if (isBind) {
            return
        }
        val intent = Intent(context, MusicService::class.java)
        // bindService 是异步操作
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        ContextCompat.startForegroundService(context, intent)
    }

    // 在绑定 Service 时监听 ExoPlayer 状态变化
    fun onPlayerStateChanged(curIsPlaying: Boolean) {
        isPlaying.value = curIsPlaying
    }

    fun playMusic(music: MusicInfo) {
        if (music !in playlistSet) return
        for (i in 0 until _playlist.value.size) {
            if (_playlist.value[i].id == music.id) {
                if (currentIndex != i) {
                    currentIndex = i
                } else {
                    return
                }
                break
            }
        }
        playCurrent()
    }

    fun playCurrent() {
        if (!isBind || playlistSet.isEmpty()) return
        val music = playlist.value[currentIndex]
        binder.playMusic(music)
        currentMusic.value = music
    }

    /**
     * 播放完毕，自动播放下一首
     */
    fun playAutoNext() {
        if (playMode.value == 0) {
            currentIndex = (currentIndex + 1) % _playlist.value.size
        } else if (playMode.value == 2) {
            var pos: Int
            do {
                pos = Random.nextInt(_playlist.value.size)
            } while (pos == currentIndex)
            currentIndex = pos
        }
        playCurrent()
    }


    fun switchPlayMode() {
        playMode.value = (playMode.value + 1) % 3
    }


    /**
     * 用户触发的播放下一曲
     * 列表循环 0、单曲循环 1 ： 播放下一首
     * 随机播放 2 ：随机播放
     */
    fun playUserNext() {
        if (playMode.value == 0 || playMode.value == 1) {
            currentIndex = (currentIndex + 1) % _playlist.value.size
        } else {
            var pos: Int
            do {
                pos = Random.nextInt(_playlist.value.size)
            } while (pos == currentIndex)
            currentIndex = pos
        }
        playCurrent()
    }

    /**
     * 用户触发的播放上一曲
     * 列表循环 0、单曲循环 1 ： 播放下一首
     * 随机播放 2 ：随机播放
     */
    fun playUserPrev() {
        if (playMode.value == 0 || playMode.value == 1) {
            currentIndex = (_playlist.value.size + currentIndex - 1) % _playlist.value.size
        } else {
            var pos: Int
            do {
                pos = Random.nextInt(_playlist.value.size)
            } while (pos == currentIndex)
            currentIndex = pos
        }
        playCurrent()
    }


    fun pauseOrResume() {
        if (isPlaying.value) {
            binder.pause()
        } else {
            binder.resume()
        }
    }

    fun seekTo(ms: Int) = binder.seekTo(ms.toLong())

    fun getDuration() = binder.duration().toInt()

    fun getCurrentPosition() = binder.getCurrentPosition().toInt()

    fun addOne(music: MusicInfo) {
        // 去重成功再加入 list 中
        if (playlistSet.add(music)) {
            _playlist.value = _playlist.value + music
        }
    }

    fun addPlayList(list: List<MusicInfo>) {
        for (music in list) {
            addOne(music)
        }
    }

    fun initPlayList(list: List<MusicInfo>) {
        playlistSet.clear()
        _playlist.value = emptyList()
        addPlayList(list)
        currentIndex = 0
        playCurrent()
    }

    fun remove(pos: Int) {
        if (pos !in _playlist.value.indices) return
        val music = _playlist.value[pos]

        // 删除音乐
        playlistSet.remove(music)
        _playlist.value = _playlist.value - music

        if (pos == currentIndex) {
            // 如果删除的是当前播放的音乐
            when {
                // a) 删完没了
                _playlist.value.isEmpty() -> {
                    binder.pause()
                    currentIndex--
                }
                // b) 顺序 / 单曲循环
                playMode.value == 0 || playMode.value == 1 -> {
                    // 如果删掉的是最后一个，回到 0，否则 currentIndex 保持 pos（因为后面项自动顶上来）
                    val next = if (pos >= _playlist.value.size) 0 else pos
                    currentIndex = next
                    playCurrent()
                }
                // c) 随机模式
                playMode.value == 2 -> {
                    currentIndex = (_playlist.value.indices).random()
                    playCurrent()
                }
            }
        } else if (pos < currentIndex) {
            // 删掉的音乐在当前播放音乐之前
            currentIndex--
        }
    }

    fun switchAndPlay(pos: Int) {
        if (pos !in _playlist.value.indices || pos == currentIndex) {
            return
        }
        currentIndex = pos
        playCurrent()
    }

}