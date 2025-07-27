package com.tonydon.music_tangjian

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.tonydon.music_tangjian.adapter.ItemAdapter
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.io.MusicRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    val gson = Gson()
    val client = OkHttpClient()
    lateinit var rvContent: RecyclerView
    lateinit var itemAdapter: ItemAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    var isLoading = false
    var current = 1
    val maxSize = 9
    val size = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        rvContent = findViewById(R.id.rv_content)
        // ItemAdapter 设置播放回调
        itemAdapter = ItemAdapter { infoList, pos ->
            val intent = Intent(this, AudioPlayActivity::class.java)
            intent.putParcelableArrayListExtra("infoList", ArrayList(infoList))
            intent.putExtra("pos", pos)
            startActivity(intent)
        }
        rvContent.adapter = itemAdapter
        val layoutManager = LinearLayoutManager(this)
        rvContent.layoutManager = layoutManager



        swipeRefreshLayout = findViewById(R.id.swipe)
        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            current = 1
            fetchData()
        }
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
                itemAdapter.submitList(res.data.records)
                swipeRefreshLayout.isRefreshing = false
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
                itemAdapter.addAll(res.data.records)
                swipeRefreshLayout.isRefreshing = false
            }
            Log.d("music", res.data.records.toString())
        }
    }
}