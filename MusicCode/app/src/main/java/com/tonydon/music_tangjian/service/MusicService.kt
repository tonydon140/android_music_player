package com.tonydon.music_tangjian.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.tonydon.music_tangjian.io.MusicInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MusicService() : Service() {

    private var playMode = 0
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val binder = MusicBinder()

    // 去重集合
    val playlistSet = LinkedHashSet<MusicInfo>()

    // 播放用的有序 List
    val playlist: MutableList<MusicInfo> = mutableListOf()
    private var isUserSwitch = false


    // 当前播放音乐的索引
    private var currentIndex = 0

    private var onPreparedListeners = mutableListOf<OnPreparedListener>()
    private var onStartListeners = mutableListOf<OnStartListener>()
    private var onPauseListeners = mutableListOf<OnPauseListener>()
    private var onPlayModeChangedListeners = mutableListOf<OnPlayModeChangedListener>()
    private var onPlayListEmptyListeners = mutableListOf<OnPlayListEmptyListener>()

    fun interface OnPreparedListener {
        fun onPrepared(music: MusicInfo)
    }

    fun interface OnStartListener {
        fun onStart()
    }

    fun interface OnPauseListener {
        fun onPause()
    }

    fun interface OnPlayModeChangedListener {
        fun onChanged(playMode: Int)
    }

    fun interface OnPlayListEmptyListener {
        fun onEmpty()
    }


    inner class MusicBinder : Binder() {
        fun removeOnPauseListener(listener: OnPauseListener) {
            onPauseListeners.remove(listener)
        }

        fun addOnPreparedListener(listener: OnPreparedListener) {
            onPreparedListeners.add(listener)
        }

        fun addOnStartListener(listener: OnStartListener) {
            onStartListeners.add(listener)
        }

        fun addOnPauseListener(listener: OnPauseListener) {
            onPauseListeners.add(listener)
        }

        fun addOnPlayModeChangedListener(listener: OnPlayModeChangedListener) {
            onPlayModeChangedListeners.add(listener)
        }

        fun addOnPlayListEmptyListener(listener: OnPlayListEmptyListener) {
            onPlayListEmptyListeners.add(listener)
        }

        fun removeOnPreparedListener(listener: OnPreparedListener) {
            onPreparedListeners.remove(listener)
        }

        fun removeOnStartListener(listener: OnStartListener) {
            onStartListeners.remove(listener)
        }

        fun removeOnPlayModeChangedListener(listener: OnPlayModeChangedListener) {
            onPlayModeChangedListeners.remove(listener)
        }

        fun removeOnPlayListEmptyListener(listener: OnPlayListEmptyListener) {
            onPlayListEmptyListeners.remove(listener)
        }


        fun playCurrent(isUser: Boolean = true) {
            val music = playlist.getOrNull(currentIndex) ?: return
            if (isUser) isUserSwitch = true
            // 开启协程，加载音乐
            CoroutineScope(Dispatchers.IO).launch {
                // 加锁，同时只能有一个就行加载资源
                mediaLock.withLock {
                    mediaPlayer.reset()
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.setDataSource(music.musicUrl) // 可能阻塞
                    mediaPlayer.prepareAsync() // 异步准备
                }
            }
        }

        fun switchPlayMode() {
            playMode = (playMode + 1) % 3
            for (listener in onPlayModeChangedListeners) {
                listener.onChanged(playMode)
            }
        }

        /**
         * 用户触发的播放下一曲
         * 列表循环 0、单曲循环 1 ： 播放下一首
         * 随机播放 2 ：随机播放
         */
        fun playUserNext() {
            if (playMode == 0 || playMode == 1) {
                currentIndex = (currentIndex + 1) % playlist.size
            } else {
                var pos: Int
                do {
                    pos = Random.nextInt(playlist.size)
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
            if (playMode == 0 || playMode == 1) {
                currentIndex = (playlist.size + currentIndex - 1) % playlist.size
            } else {
                var pos: Int
                do {
                    pos = Random.nextInt(playlist.size)
                } while (pos == currentIndex)
                currentIndex = pos
            }
            playCurrent()
        }

//        /**
//         * 软件自动的下一曲
//         *
//         */
//        fun playNext() {
//            isUserSwitch = true
//            if (playMode == 0) {
//                currentIndex = (currentIndex + 1) % playlist.size
//            } else if (playMode == 2) {
//                var pos: Int
//                do {
//                    pos = Random.nextInt(playlist.size)
//                } while (pos == currentIndex)
//                currentIndex = pos
//            }
//            playCurrent()
//        }
//
//        /**
//         * 软件自动的上一曲
//         */
//        fun playPrev() {
//            if (playMode == 0) {
//                currentIndex = (playlist.size + currentIndex - 1) % playlist.size
//            } else if (playMode == 2) {
//                var pos: Int
//                do {
//                    pos = Random.nextInt(playlist.size)
//                } while (pos == currentIndex)
//                currentIndex = pos
//            }
//            playCurrent()
//        }

        fun pauseOrResume() {
            if (mediaPlayer.isPlaying) {
                pause()
            } else {
                resume()
            }
        }

        fun pause() {
            mediaPlayer.pause()
            for (listener in onPauseListeners) {
                listener.onPause()
            }
        }

        fun resume() {
            mediaPlayer.start()
            for (listener in onStartListeners) {
                listener.onStart()
            }
        }


        fun seekTo(ms: Int) = mediaPlayer.seekTo(ms)
        fun getDuration() = mediaPlayer.duration
        fun getCurrentPosition() = mediaPlayer.currentPosition

        fun isPlaying() = mediaPlayer.isPlaying

        fun getPlayMode() = playMode

        fun getCurrentMusic(): MusicInfo {
            return playlist[currentIndex]
        }

        fun addOne(music: MusicInfo) {
            // 去重成功再加入 list 中
            if (playlistSet.add(music)) {
                playlist.add(music)
            }
        }

        fun addPlayList(list: List<MusicInfo>) {
            for (music in list) {
                addOne(music)
            }
        }

        fun initPlayList(list: List<MusicInfo>) {
            playlistSet.clear()
            playlist.clear()
            addPlayList(list)
            Log.d("music_service", list.toString())
            currentIndex = 0
            playCurrent()
        }

        fun remove(pos: Int) {
            if (pos !in playlist.indices) return
            // 删除音乐
            playlistSet.remove(playlist[pos])
            playlist.removeAt(pos)

            if (pos == currentIndex) {
                // 如果删除的是当前播放的音乐
                when {
                    // a) 删完没了
                    playlist.isEmpty() -> {
                        pause()
                        for (listener in onPlayListEmptyListeners) {
                            listener.onEmpty()
                        }
                    }
                    // b) 顺序 / 单曲循环
                    playMode == 0 || playMode == 1 -> {
                        // 如果删掉的是最后一个，回到 0，否则 currentIndex 保持 pos（因为后面项自动顶上来）
                        val next = if (pos >= playlist.size) 0 else pos
                        currentIndex = next
                        playCurrent()
                    }
                    // c) 随机模式
                    playMode == 2 -> {
                        currentIndex = (playlist.indices).random()
                        playCurrent()
                    }
                }
            } else if (pos < currentIndex) {
                // 删掉的音乐在当前播放音乐之前
                currentIndex--
            }

        }

        fun switchAndPlay(pos: Int) {
            if (pos !in playlist.indices || pos == currentIndex) {
                return
            }
            currentIndex = pos
            playCurrent()
        }

        fun playMusic(music: MusicInfo) {
            if (music !in playlistSet) return
            isUserSwitch = true
            for (i in 0 until playlist.size) {
                if (playlist[i].id == music.id) {
                    if (i == currentIndex) {
                        return
                    } else {
                        currentIndex = i
                    }
                    break
                }
            }
            playCurrent()
        }

        fun getPlayList() = playlist.toList()
        fun getPlayListSize() = playlist.size
    }


    override fun onCreate() {
        super.onCreate()
        // 音乐准备完毕回调
        mediaPlayer.setOnPreparedListener {
            it.start() // 回到主线程启动播放
            isUserSwitch = false
            for (listener in onPreparedListeners) {
                listener.onPrepared(playlist[currentIndex])
            }
            for (listener in onStartListeners) {
                listener.onStart()
            }
        }
        // 音乐播放完毕回调
        mediaPlayer.setOnCompletionListener {
            if (!isUserSwitch) {
                isUserSwitch = true
                if (playMode == 0) {
                    currentIndex = (currentIndex + 1) % playlist.size
                } else if (playMode == 2) {
                    var pos: Int
                    do {
                        pos = Random.nextInt(playlist.size)
                    } while (pos == currentIndex)
                    currentIndex = pos
                }
                binder.playCurrent(isUser = false)
            }
            Log.d("music_completion_user", "current = $currentIndex")
        }
    }

    private val mediaLock = Mutex()


    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}