package com.tonydon.music_tangjian

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.tonydon.music_tangjian.adapter.HomeItemAdapter
import com.tonydon.music_tangjian.fragment.MusicListBottomSheet
import com.tonydon.music_tangjian.http.RetrofitClient
import com.tonydon.music_tangjian.service.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var rvContent: RecyclerView
    lateinit var homeItemAdapter: HomeItemAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    var isLoading = false
    var current = 1
    val maxSize = 9
    val size = 4
    lateinit var nameTV: TextView
    lateinit var authorTV: TextView
    lateinit var coverIV: ImageView
    lateinit var playIB: ImageButton
    lateinit var listIB: ImageButton
    lateinit var bottomView: ConstraintLayout
    lateinit var deepseekIV: ImageView


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 请求通知权限
        requestPermission()

        // 获取 View 实例
        nameTV = findViewById(R.id.tv_bottom_name)
        authorTV = findViewById(R.id.tv_bottom_author)
        coverIV = findViewById(R.id.iv_bottom_cover)
        playIB = findViewById(R.id.ib_home_play)
        listIB = findViewById(R.id.ib_bottom_list)
        bottomView = findViewById(R.id.floating_view)
        deepseekIV = findViewById(R.id.iv_deepseek)
        rvContent = findViewById(R.id.rv_content)

        // 监听 StateFlow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 监听音乐切换
                launch {
                    PlayerManager.currentMusic.collect { music ->
                        bottomView.visibility = View.VISIBLE
                        nameTV.text = music?.musicName
                        val tmp = "-${music?.author}"
                        authorTV.text = tmp
                        Glide.with(coverIV)
                            .load(music?.coverUrl)
                            .circleCrop()
                            .into(coverIV)
                    }
                }
                // 监听播放状态
                launch {
                    PlayerManager.isPlaying.collect { isPlaying ->
                        if (isPlaying) {
                            playIB.setImageResource(R.drawable.ic_home_pause) // 切换为播放图标
                        } else {
                            playIB.setImageResource(R.drawable.ic_home_play) // 切换为播放图标
                        }
                    }
                }
                // 监听播放列表
                launch {
                    PlayerManager.playlist.collect { playlist ->
                        if (playlist.isEmpty()) {
                            bottomView.visibility = View.GONE
                        } else {
                            bottomView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        // ItemAdapter 设置播放回调
        homeItemAdapter = HomeItemAdapter { infoList, pos ->
            PlayerManager.addPlayList(infoList)
            PlayerManager.switchAndPlay(infoList[pos].id)
            val intent = Intent(this, AudioPlayActivity::class.java)
            startActivity(intent)
        }
        rvContent.adapter = homeItemAdapter
        val layoutManager = LinearLayoutManager(this)
        rvContent.layoutManager = layoutManager

        // 下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipe)
        swipeRefreshLayout.setOnRefreshListener {
            current = 1
            fetchData()
        }
        // 请求数据
        fetchData()

        // 上拉加载更多
        rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy <= 0) {
                    return
                }
                val pos = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && pos >= totalItemCount - 2) {
                    loadMore()
                }
            }
        })


        playIB.setOnClickListener { PlayerManager.pauseOrResume() }

        // 弹出音乐列表
        listIB.setOnClickListener {
            MusicListBottomSheet().show(supportFragmentManager, "MusicList")
        }

        // 点击悬浮 View 进入播放页面
        bottomView.setOnClickListener {
            val intent = Intent(this, AudioPlayActivity::class.java)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.slide_in_up,
                0
            )
        }
        // 点击 AI 按钮，加入 AI 页面
        deepseekIV.setOnClickListener {
            val intent = Intent(this, DeepseekActivity::class.java)
            startActivity(intent)
        }
    }

    fun fetchData() {
        // 启动协程，可以在主线程启动，Retrofit 会自动切换到 IO 线程
        lifecycleScope.launch {
            try {
                // 直接调用 suspend 函数获取数据
                val musicRes = RetrofitClient.musicApi.query(current, size)

                // 成功后的业务逻辑
                current += 1
                val newRecords = musicRes.data.records
                Log.d("music", newRecords.toString())

                // 切换到主线程更新 UI
                withContext(Dispatchers.Main) {
                    homeItemAdapter.submitList(newRecords)
                    swipeRefreshLayout.isRefreshing = false

                    // 添加随机的模块音乐
                    newRecords.randomOrNull()?.let {
                        PlayerManager.initPlayList(it.musicInfoList)
                    }
                }
            } catch (e: Exception) {
                Log.e("music", "网络请求出错", e)
                withContext(Dispatchers.Main) {
                    // 也可以在 UI 上给用户提示，比如一个 Toast
                    Toast.makeText(this@MainActivity, "加载失败，请检查网络", Toast.LENGTH_SHORT)
                        .show()
                    swipeRefreshLayout.isRefreshing = false // 别忘了在出错时也要停止刷新动画
                }
            }
        }
    }

    fun loadMore() {
        if (isLoading || (current - 1) * size > maxSize) {
            return
        }
        isLoading = true

        // 开启协程加载更多
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.musicApi.query(current, size)
                current++
                val newRecords = res.data.records
                withContext(Dispatchers.Main) {
                    homeItemAdapter.addAll(newRecords)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("music", "加载更多失败", e)
                    Toast.makeText(applicationContext, "加载更多失败", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
                withContext(Dispatchers.Main) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
}