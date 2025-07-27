package com.tonydon.music_tangjian.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MusicService() : Service() {

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val binder = MusicBinder()

    private var musicPlayListener: MusicPlayListener? = null


    interface MusicPlayListener {
        fun onPrepared()
        fun onCompletion()
    }


    inner class MusicBinder : Binder() {
        fun pause() = mediaPlayer.pause()
        fun resume() = mediaPlayer.start()
        fun switchAndPlay(url: String) = playInternal(url)
        fun isPlaying() = mediaPlayer.isPlaying
        fun seekTo(ms: Int) = mediaPlayer.seekTo(ms)
        fun getDuration() = mediaPlayer.duration
        fun getCurrentPosition() = mediaPlayer.currentPosition
        fun setMusicPlayListener(listener: MusicPlayListener) {
            musicPlayListener = listener
        }
    }


    override fun onCreate() {
        super.onCreate()
        // 音乐准备完毕回调
        mediaPlayer.setOnPreparedListener {
            it.start() // 回到主线程启动播放
            musicPlayListener?.onPrepared()
        }
        // 音乐播放完毕回调
        mediaPlayer.setOnCompletionListener {
            musicPlayListener?.onCompletion()
        }
    }

    private val mediaLock = Mutex()

    private fun playInternal(url: String) {
        // 开启协程，加载音乐
        CoroutineScope(Dispatchers.IO).launch {
            // 加锁，同时只能有一个就行加载资源
            mediaLock.withLock {
                mediaPlayer.reset()
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setDataSource(url) // 可能阻塞
                mediaPlayer.prepareAsync() // 异步准备
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}