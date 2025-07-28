package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.MusicListAdapter
import com.tonydon.music_tangjian.service.PlayerManager

class MusicListBottomSheet : BottomSheetDialogFragment() {
    lateinit var adapter: MusicListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvMusicList)
        rv.layoutManager = LinearLayoutManager(context)

        adapter = MusicListAdapter({ pos ->
            // 点击播放回调
            PlayerManager.binder.switchAndPlay(pos)
        }, { pos ->
            // 点击删除回调
            PlayerManager.binder.remove(pos)
            adapter.submitList(PlayerManager.binder.getPlayList())
        })
        adapter.submitList(PlayerManager.binder.getPlayList())
        adapter.setPlayingId(PlayerManager.binder.getCurrentMusic().id)
        rv.adapter = adapter

        // 监听音乐
        PlayerManager.binder.addOnPreparedListener { music ->
            adapter.setPlayingId(music.id)
        }
    }

}