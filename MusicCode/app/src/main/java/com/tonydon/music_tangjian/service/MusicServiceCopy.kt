package com.tonydon.music_tangjian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.tonydon.music_tangjian.MainActivity
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.utils.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MusicServiceCopy() : Service() {

    private var playMode = 0
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var exoPlayer: ExoPlayer

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
            exoPlayer.setMediaItem(MediaItem.fromUri(music.musicUrl))   // 设置 URI
            exoPlayer.prepare()  // 异步准备
            exoPlayer.playWhenReady = true
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


        fun pauseOrResume() {
            if (exoPlayer.isPlaying) {
                pause()
            } else {
                resume()
            }
        }

        fun pause() {
            exoPlayer.pause()
//            mediaPlayer.pause()
            for (listener in onPauseListeners) {
                listener.onPause()
            }
        }

        fun resume() {
            exoPlayer.play()
//            mediaPlayer.start()
            for (listener in onStartListeners) {
                listener.onStart()
            }
        }


        fun seekTo(ms: Int) = exoPlayer.seekTo(ms.toLong())
        fun getDuration() = exoPlayer.duration.toInt()
        fun getCurrentPosition() = exoPlayer.currentPosition.toInt()

        fun isPlaying() = exoPlayer.isPlaying

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
                        currentIndex--
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

        fun getPlayList() = playlist.toList()
        fun getPlayListSize() = playlist.size
    }


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        // 构建 DefaultMediaSourceFactory，并注入到播放器
        val factory = DefaultMediaSourceFactory(
            CacheManager.buildCacheDataSourceFactory(applicationContext)
        )

        // 构建播放器
        exoPlayer = ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(factory)
            .build()
        exoPlayer.playWhenReady = true  // 准备好后自动播放

        // 播放器监听
        exoPlayer.addListener(object : Player.Listener {
            /**
             * 播放状态的监听
             */
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        // 正在缓冲/加载
                    }

                    Player.STATE_READY -> {
                        // 媒体已加载完毕并可播放
                        isUserSwitch = false
                        updateNotification() // 更新通知
                        // 调用 onPrepared 监听
                        for (listener in onPreparedListeners) {
                            listener.onPrepared(playlist[currentIndex])
                        }
                    }

                    Player.STATE_ENDED -> {
                        // 播放结束
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

                    Player.STATE_IDLE -> {
                        // 未开始或已停止
                    }
                }
            }

            /**
             * 开始播放的监听
             */
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    for (listener in onStartListeners) {
                        listener.onStart()
                    }
                }
            }
        })
//        exoPlayer.addListener(object : Player)
//        // 音乐准备完毕回调
//        mediaPlayer.setOnPreparedListener {
//            it.start() // 回到主线程启动播放
//
//            Log.d("music_OnPrepared", "currentIndex = $currentIndex")
//
//
//        }
//        // 音乐播放完毕回调
//        mediaPlayer.setOnCompletionListener {
//            if (!isUserSwitch) {
//                isUserSwitch = true
//                if (playMode == 0) {
//                    currentIndex = (currentIndex + 1) % playlist.size
//                } else if (playMode == 2) {
//                    var pos: Int
//                    do {
//                        pos = Random.nextInt(playlist.size)
//                    } while (pos == currentIndex)
//                    currentIndex = pos
//                }
//                binder.playCurrent(isUser = false)
//            }
//            Log.d("music_completion_user", "current = $currentIndex")
//        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createPlaybackNotification()
        startForeground(PLAYBACK_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val PLAYBACK_NOTIFICATION_ID = 1

    private fun createPlaybackNotification(): Notification {
        // 常见通知渠道
        val channelId = "music_playback"
        val channel = NotificationChannel(
            channelId,
            "音乐播放",
            NotificationManager.IMPORTANCE_LOW
        )
        // 注册通知渠道
        notificationManager.createNotificationChannel(channel)

        // 创建 PendingIntent 跳转到 MainActivity
        val clickIntent = Intent(this, MainActivity::class.java).apply {
//            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("正在播放")
            .setContentText("-")
            .setSmallIcon(R.drawable.ic_launcher_music)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }


    fun updateNotification() {
        // 创建 PendingIntent 跳转到 MainActivity
        val clickIntent = Intent(this, MainActivity::class.java).apply {
//            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "music_playback"
        val music = playlist[currentIndex]
        val newNotification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("正在播放")
            .setContentText("${music.musicName} - ${music.author}")  // 更新歌曲标题
            .setSmallIcon(R.drawable.ic_launcher_music)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        notificationManager.notify(PLAYBACK_NOTIFICATION_ID, newNotification)
    }


    private val mediaLock = Mutex()


    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        exoPlayer.release()
        super.onDestroy()
    }
}