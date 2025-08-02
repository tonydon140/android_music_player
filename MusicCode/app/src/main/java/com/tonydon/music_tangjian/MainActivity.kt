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
import com.google.gson.Gson
import com.tonydon.music_tangjian.adapter.HomeItemAdapter
import com.tonydon.music_tangjian.fragment.MusicListBottomSheet
import com.tonydon.music_tangjian.data.MusicRes
import com.tonydon.music_tangjian.service.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    val gson = Gson()
    val client = OkHttpClient()
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
                        if (playlist.isEmpty()){
                            bottomView.visibility = View.GONE
                        }else{
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
                    isLoading = true
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
        lifecycleScope.launch(Dispatchers.IO) {
            val url =
                "https://hotfix-service-prod.g.mi.com/music/homePage?current=${current}&size=${size}"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("music", "网络请求出错：${response.message}")
                return@launch
            }
            current += 1
            val body = response.body?.string()
            val res: MusicRes = gson.fromJson(body, MusicRes::class.java)
            runOnUiThread {
                homeItemAdapter.submitList(res.data.records)
                swipeRefreshLayout.isRefreshing = false
                // 添加随机的模块音乐
                val randomMusicList = res.data.records.random().musicInfoList
                PlayerManager.initPlayList(randomMusicList)
            }
            Log.d("music", res.data.records.toString())
        }
    }

    fun loadMore() {
        Log.d("music", "$current")
        if ((current - 1) * size > maxSize) {
            isLoading = false
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val url =
                "https://hotfix-service-prod.g.mi.com/music/homePage?current=${current}&size=${size}"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .get()
                .build()

            // 获取请求
            val response = client.newCall(request).execute()
            isLoading = false

            if (!response.isSuccessful) {
                Log.e("music", "网络请求出错：${response.message}")
                return@launch
            }

            current++
            val body = response.body?.string()
            val res: MusicRes = gson.fromJson(body, MusicRes::class.java)
            runOnUiThread {
                homeItemAdapter.addAll(res.data.records)
                swipeRefreshLayout.isRefreshing = false
            }
            Log.d("music", res.data.records.toString())
        }
    }
}