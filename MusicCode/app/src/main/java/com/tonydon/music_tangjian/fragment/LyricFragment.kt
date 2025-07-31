package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dirror.lyricviewx.LyricViewX
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.service.PlayerManager
import kotlinx.coroutines.launch


class LyricFragment : Fragment() {

    private lateinit var lyricViewX: LyricViewX

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lyric, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lyricViewX = view.findViewById(R.id.lyricViewX)
        lifecycleScope.launch {
            PlayerManager.currentMusic.collect { music ->
                if (music != null) {
                    updateLyric(music.lyricUrl)
                }
            }
        }
    }

    fun updateLyric(url: String) {
        lyricViewX.loadLyricByUrl(url, "utf-8")
    }

    fun updateTime(time: Long) {
        lyricViewX.updateTime(time)
    }

    fun setColor(color: Int) {
        lyricViewX.setCurrentColor(color)
    }

}