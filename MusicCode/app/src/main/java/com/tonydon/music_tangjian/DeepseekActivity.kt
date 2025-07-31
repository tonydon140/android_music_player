package com.tonydon.music_tangjian

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tonydon.music_tangjian.fragment.ChatFragment
import com.tonydon.music_tangjian.fragment.HistoryFragment
import com.tonydon.music_tangjian.utils.AndroidBug5497Workaround

class DeepseekActivity : AppCompatActivity() {

    lateinit var viewPager2: ViewPager2
    lateinit var tabLayout: TabLayout
    val titles = listOf("对话", "历史")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_deepseek)
        AndroidBug5497Workaround.assistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val paddingBottom = if (imeVisible) 0 else systemBars.bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, paddingBottom)
            insets
        }

        viewPager2 = findViewById(R.id.ai_view_pager2)
        tabLayout = findViewById(R.id.ai_tab_layout)

        // 设置适配器
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = titles.size
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ChatFragment()
                    1 -> HistoryFragment()
                    else -> ChatFragment()
                }
            }
        }

        // TabLayout 和 ViewPager2 绑定
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}