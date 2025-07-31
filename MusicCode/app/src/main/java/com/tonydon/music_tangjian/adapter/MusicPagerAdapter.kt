package com.tonydon.music_tangjian.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonydon.music_tangjian.fragment.CoverFragment
import com.tonydon.music_tangjian.fragment.LyricFragment

class MusicPagerAdapter(
    fragment: FragmentActivity
) : FragmentStateAdapter(fragment) {

    val coverFragment = CoverFragment()
    val lyricFragment = LyricFragment()

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> coverFragment
            1 -> lyricFragment
            else -> throw IllegalArgumentException("Invalid page")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}