package com.tonydon.music_tangjian

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.adapter.MusicPagerAdapter
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.service.MusicService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class AudioPlayActivity : AppCompatActivity() {

    lateinit var playButton: ImageButton
    lateinit var nextButton: ImageButton
    lateinit var prevButton: ImageButton
    lateinit var switchTypeButton: ImageButton
    lateinit var favoriteButton: ImageButton
    lateinit var closeButton: ImageButton
    lateinit var seekBar: SeekBar
    lateinit var nameTV: TextView
    lateinit var authorTV: TextView
    lateinit var seekStartTimeTV: TextView
    lateinit var seekEndTimeTV: TextView

    lateinit var musicInfoList: List<MusicInfo>
    lateinit var viewPager: ViewPager2
    lateinit var adapter: MusicPagerAdapter
    lateinit var root: LinearLayout
    var pos = 0

    var playType = 0 // 0：列表循环，1：单曲循环，2：随机播放

    lateinit var musicService: MusicService.MusicBinder
    var isServiceBind = false
    val playTypeImageIds = listOf(
        R.drawable.ic_cycle_list,
        R.drawable.ic_repeat,
        R.drawable.ic_random
    )
    val mmkv = MMKV.defaultMMKV()


    private val serviceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            musicService = service as MusicService.MusicBinder
            isServiceBind = true
            musicService.setMusicPlayListener(object : MusicService.MusicPlayListener {
                // 音乐准备完毕，设置一些信息
                override fun onPrepared() {
                    playButton.setImageResource(R.drawable.ic_pause) // 切换为暂停图标
                    seekBar.max = musicService.getDuration()
                    seekEndTimeTV.text = formatTime(musicService.getDuration())
                }

                // 播放完毕，播放下一首
                override fun onCompletion() {
                    if (playType == 1) {
                        playItem()
                    } else {
                        playNext()
                    }
                }
            })
            // 开启播放
            playItem()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBind = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_audio_play)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getMusicInfo()
        pos = intent.getIntExtra("pos", 0)

        // 获取视图
        root = findViewById(R.id.main_audio_play)
        viewPager = findViewById(R.id.audio_viewpager)
        playButton = findViewById(R.id.btn_play)
        nextButton = findViewById(R.id.btn_to_right)
        prevButton = findViewById(R.id.btn_to_left)
        switchTypeButton = findViewById(R.id.btn_play_type)
        closeButton = findViewById(R.id.btn_close)
        favoriteButton = findViewById(R.id.btn_favorite)
        nameTV = findViewById(R.id.tv_audio_name)
        authorTV = findViewById(R.id.tv_audio_author)
        seekStartTimeTV = findViewById(R.id.tv_seek_cur_time)
        seekEndTimeTV = findViewById(R.id.tv_seek_end_time)
        seekBar = findViewById(R.id.sb_audio)
        seekBar.min = 0

        adapter = MusicPagerAdapter(musicInfoList[pos], this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        // 播放音乐
        playButton.setOnClickListener {
            if (musicService.isPlaying()) {
                // 当前在播放，设置为暂停
                musicService.pause()
                playButton.setImageResource(R.drawable.ic_play) // 切换为播放图标
                adapter.coverFragment.stopAnim()
            } else {
                // 当前在暂停，设置为播放
                musicService.resume()
                playButton.setImageResource(R.drawable.ic_pause) // 切换为暂停图标
                adapter.coverFragment.resumeAnim()
            }
        }
        startPlayMusic(pos) // 打开页面就开始播放音乐
        updateProgressUI()  // 定时更新进度

        nextButton.setOnClickListener { playNext() }
        prevButton.setOnClickListener { playPrev() }

        switchTypeButton.setOnClickListener {
            playType = (playType + 1) % 3
            switchTypeButton.setImageResource(playTypeImageIds[playType])
        }

        // 设置收藏按钮
        favoriteButton.setOnClickListener {
            val music = musicInfoList[pos]
            val key = "${music.id}_fav"
            val fav = !mmkv.decodeBool(key, false)
            mmkv.encode(key, fav)
            if (fav) {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_not_favorite)
            }
        }

        // 设置进度条拖动监听
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isServiceBind && seekBar != null) {
                    musicService.seekTo(seekBar.progress)
                }
            }
        })

        // 设置关闭动画
        closeButton.setOnClickListener {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(
                0,
                R.anim.slide_out_down
            )
        }
    }


    /**
     * 启动 Activity 时播放选择的音乐
     */
    fun startPlayMusic(pos: Int) {
        val music = musicInfoList[pos]
        Log.d("music_play", "pos = $pos, name = ${music.musicName}")

        // 设置服务
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("music_url", music.musicUrl)
        startService(intent)    // 启动服务，播放音乐
        bindService(intent, serviceConn, BIND_AUTO_CREATE)  // 绑定服务，控制音乐
    }

    /**
     * 播放 pos 位置的音乐
     */
    fun playItem() {
        val music = musicInfoList[pos]
        nameTV.text = music.musicName
        authorTV.text = music.author
        adapter.coverFragment.updateImage(music.coverUrl)
        adapter.lyricFragment.updateLyric(music.lyricUrl)
        setBackgroundColor(music)
        musicService.switchAndPlay(music.musicUrl)
        val fav = mmkv.decodeBool("${musicInfoList[pos].id}_fav", false)
        favoriteButton.setImageResource(if (fav) R.drawable.ic_favorite else R.drawable.ic_not_favorite)
    }

    /**
     * 根据不同的播放类型，播放下一首音乐
     */
    fun playNext() {
        if (playType == 0 || playType == 1) {
            pos = (pos + 1) % musicInfoList.size
            playItem()
        } else {
            val curPos = pos
            do {
                pos = Random.nextInt(musicInfoList.size)
            } while (pos == curPos)
            playItem()
        }
        Log.d("music_next", "type = ${playType}, pos = $pos")
    }

    fun playPrev() {
        if (playType == 0 || playType == 1) {
            pos = (musicInfoList.size + pos - 1) % musicInfoList.size
            playItem()
        } else {
            val curPos = pos
            do {
                pos = Random.nextInt(musicInfoList.size)
            } while (pos == curPos)
            playItem()
        }
        Log.d("music_prev", "type = ${playType}, pos = $pos")
    }

    /**
     * 提取图片主题色，设置背景
     */
    private fun setBackgroundColor(music: MusicInfo) {
        Glide.with(this)
            .asBitmap()
            .load(music.coverUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    Palette.from(resource).generate { palette ->
                        // 提取主色（或其他变体色）
                        val dominantColor = palette?.getDominantColor(Color.DKGRAY)  // 设置默认色防止为空
                        setEnvColor(dominantColor ?: Color.DKGRAY)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    /**
     * 根据主题色，设置环境色
     */
    fun setEnvColor(dominantColor: Int) {
        root.setBackgroundColor(dominantColor)
        if (isColorDark(dominantColor)) {
            nameTV.setTextColor(Color.WHITE)
            authorTV.setTextColor(Color.WHITE)
            seekEndTimeTV.setTextColor(Color.WHITE)
            seekStartTimeTV.setTextColor(Color.WHITE)
            adapter.lyricFragment.setColor(Color.WHITE)
            seekBar.progressTintList = ColorStateList.valueOf("#FF4081".toColorInt())
            seekBar.thumbTintList = ColorStateList.valueOf("#D81B60".toColorInt())
            seekBar.backgroundTintList = ColorStateList.valueOf("#E0E0E0".toColorInt())
        } else {
            nameTV.setTextColor(Color.BLACK)
            authorTV.setTextColor(Color.BLACK)
            seekEndTimeTV.setTextColor(Color.BLACK)
            seekStartTimeTV.setTextColor(Color.BLACK)
            adapter.lyricFragment.setColor(Color.BLACK)
            seekBar.progressTintList = ColorStateList.valueOf("#80D8FF".toColorInt())
            seekBar.thumbTintList = ColorStateList.valueOf("#00B0FF".toColorInt())
            seekBar.backgroundTintList = ColorStateList.valueOf("#424242".toColorInt())
        }
    }

    /**
     * 判断颜色是不是暗色系
     */
    fun isColorDark(color: Int): Boolean {
        val luminance = ColorUtils.calculateLuminance(color)
        return luminance < 0.5
    }

    /**
     * 从 Intent 中提取音乐信息
     */
    fun getMusicInfo() {
        val infoList =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("infoList", MusicInfo::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("infoList")
            }
        if (infoList == null) {
            finish()
        } else {
            musicInfoList = infoList
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBind) {
            unbindService(serviceConn)
            isServiceBind = false
        }
        Log.d("music_audio", "onDestroy...")
    }

    private fun updateProgressUI() {
        // 更新进度条
        lifecycleScope.launch {
            while (true) {
                delay(500)
                if (isServiceBind) {
                    val position = musicService.getCurrentPosition()
                    seekBar.progress = position
                    seekStartTimeTV.text = formatTime(position)
                    adapter.lyricFragment.updateTime(position.toLong())
                }
            }
        }
    }

    // 格式化毫秒到 mm:ss
    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d: %02d", min, sec)
    }
}