package com.tonydon.music_tangjian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.tonydon.music_tangjian.MainActivity
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.data.MusicInfo
import com.tonydon.music_tangjian.utils.CacheManager

class MusicService() : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private val binder = MusicBinder()


    inner class MusicBinder : Binder() {
        /**
         * 播放指定的音乐
         */
        fun playMusic(music: MusicInfo) {
            updateNotification(music)
            exoPlayer.setMediaItem(MediaItem.fromUri(music.musicUrl))   // 设置 URI
            exoPlayer.prepare()  // 异步准备
            exoPlayer.playWhenReady = true
        }

        /**
         * 暂停播放
         */
        fun pause() {
            exoPlayer.pause()
        }

        /**
         * 恢复播放
         */
        fun resume() {
            exoPlayer.play()
        }

        /**
         * 调整进度
         */
        fun seekTo(ms: Long) {
            exoPlayer.seekTo(ms)
        }

        fun duration(): Long {
            return exoPlayer.duration
        }

        fun getCurrentPosition() = exoPlayer.currentPosition
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
                        // 媒体已加载完毕并可播放，更新 duration
                        PlayerManager.duration.value = exoPlayer.duration
                    }

                    Player.STATE_ENDED -> {
                        // 播放结束，自动播放下一首
                        PlayerManager.playAutoNext()
                    }

                    Player.STATE_IDLE -> {
                        // 未开始或已停止
                    }
                }
            }

            /**
             * 监听播放状态变化，分发给 PlayerManager
             */
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                PlayerManager.onPlayerStateChanged(isPlaying)
            }
        })

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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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


    fun updateNotification(music: MusicInfo) {
        // 创建 PendingIntent 跳转到 MainActivity
        val clickIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "music_playback"
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


    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        exoPlayer.release()
        super.onDestroy()
    }
}