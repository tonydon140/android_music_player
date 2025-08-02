package com.tonydon.music_tangjian

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tonydon.music_tangjian.adapter.MusicPagerAdapter
import com.tonydon.music_tangjian.fragment.MusicListBottomSheet
import com.tonydon.music_tangjian.data.MusicInfo
import com.tonydon.music_tangjian.service.PlayerManager
import com.tonydon.music_tangjian.utils.ConfigUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class AudioPlayActivity : AppCompatActivity() {

    lateinit var playButton: ImageButton
    lateinit var nextButton: ImageButton
    lateinit var prevButton: ImageButton
    lateinit var switchTypeButton: ImageButton
    lateinit var favoriteButton: ImageButton
    lateinit var closeButton: ImageButton
    lateinit var displayButton: ImageButton
    lateinit var seekBar: SeekBar
    lateinit var nameTV: TextView
    lateinit var authorTV: TextView
    lateinit var seekStartTimeTV: TextView
    lateinit var seekEndTimeTV: TextView
    lateinit var viewPager: ViewPager2
    lateinit var adapter: MusicPagerAdapter
    lateinit var root: LinearLayout
    val playTypeImageIds = listOf(
        R.drawable.ic_cycle_list,
        R.drawable.ic_repeat,
        R.drawable.ic_random
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_audio_play)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        displayButton = findViewById(R.id.btn_display)

        // 配置封面图和歌词页面
        adapter = MusicPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        // 播放控制
        playButton.setOnClickListener { PlayerManager.pauseOrResume() }
        nextButton.setOnClickListener { PlayerManager.playUserNext() }
        prevButton.setOnClickListener { PlayerManager.playUserPrev() }
        switchTypeButton.setOnClickListener { PlayerManager.switchPlayMode() }

        lifecycleScope.launch {
            // 在生命周期为 STARTED 或更高时运行
            // 生命周期低于 STARTED 会自动挂起
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // 自动监听 playMode 的变化
                    PlayerManager.playMode.collect { mode ->
                        switchTypeButton.setImageResource(playTypeImageIds[mode])
                    }
                }
                launch {
                    // 自动监听 isPlaying 的变化
                    PlayerManager.isPlaying.collect { isPlaying ->
                        if (isPlaying) {
                            playButton.setImageResource(R.drawable.ic_pause)
                        } else {
                            playButton.setImageResource(R.drawable.ic_play)
                        }
                    }
                }
                // 自动监听 music 的变化
                launch {
                    PlayerManager.currentMusic.collect { music ->
                        if (music != null) {
                            setupUI(music)
                        }
                    }
                }
                // 自动监听 playlist 的变化
                launch {
                    PlayerManager.playlist.collect { playlist ->
                        if (playlist.isEmpty()) {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
                // 自动监听 duration 的变化
                launch {
                    PlayerManager.duration.collect { duration ->
                        seekBar.max = duration.toInt()
                        seekEndTimeTV.text = formatTime(duration.toInt())
                    }
                }
            }
        }

        // 打开音乐列表
        displayButton.setOnClickListener {
            MusicListBottomSheet().show(supportFragmentManager, "MusicList")
        }

        // 设置收藏按钮
        favoriteButton.setOnClickListener { setFavorite() }

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
                if (seekBar != null) {
                    PlayerManager.seekTo(seekBar.progress)
                }
            }
        })

        // 设置关闭动画
        closeButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(
                    0,
                    R.anim.slide_out_down
                )
            }
        })

        updateProgressUI()  // 定时更新进度
    }

    /**
     * 根据播放音乐更新 UI
     */
    private fun setupUI(music: MusicInfo) {
        // 文字信息
        nameTV.text = music.musicName
        authorTV.text = music.author
        // 设置进度条
        seekBar.min = 0
        seekBar.max = PlayerManager.getDuration()
        seekEndTimeTV.text = formatTime(PlayerManager.getDuration())
        // 设置背景
        setBackgroundColor(music)
        // 设置是否收藏
        val fav = ConfigUtils.isFavorite(music)
        favoriteButton.setImageResource(if (fav) R.drawable.ic_favorite else R.drawable.ic_not_favorite)
    }

    private fun setFavorite() {
        val music = PlayerManager.currentMusic.value ?: return
        val oldIsFav = ConfigUtils.isFavorite(music)
        ConfigUtils.setFavorite(music, !oldIsFav)
        if (oldIsFav) {
            favoriteButton.setImageResource(R.drawable.ic_not_favorite)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite)
        }
        // 准备动画
        val animSet = AnimatorSet()

        if (!oldIsFav) {
            // a. 点击收藏：1.0 → 1.2 → 1.0 缩放 + Y 轴 360° 旋转
            val scaleX = ObjectAnimator.ofFloat(favoriteButton, "scaleX", 1f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(favoriteButton, "scaleY", 1f, 1.2f, 1f)
            val rotateY = ObjectAnimator.ofFloat(favoriteButton, "rotationY", 0f, 360f)
            animSet.playTogether(scaleX, scaleY, rotateY)
        } else {
            // b. 点击取消收藏：1.0 → 0.8 → 1.0 缩放
            val scaleX = ObjectAnimator.ofFloat(favoriteButton, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(favoriteButton, "scaleY", 1f, 0.8f, 1f)
            animSet.playTogether(scaleX, scaleY)
        }

        animSet.duration = 1000L
        animSet.start()
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


    // 更新进度条
    private fun updateProgressUI() {
        lifecycleScope.launch {
            while (true) {
                delay(500)
                val position = PlayerManager.getCurrentPosition()
                seekBar.progress = position
                seekStartTimeTV.text = formatTime(position)
                adapter.lyricFragment.updateTime(position.toLong())
            }
        }
    }

    // 格式化毫秒到 mm:ss
    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.US, "%02d: %02d", min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}