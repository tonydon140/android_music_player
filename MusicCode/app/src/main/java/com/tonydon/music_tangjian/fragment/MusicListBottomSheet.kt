package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.MusicListAdapter
import com.tonydon.music_tangjian.service.PlayerManager

class MusicListBottomSheet : BottomSheetDialogFragment() {
    lateinit var adapter: MusicListAdapter
    private val iconList = listOf(
        R.drawable.ic_list_type_cycle,
        R.drawable.ic_list_type_repeat,
        R.drawable.ic_list_type_random,
    )
    private val textList = listOf(
        "顺序播放",
        "单曲循环",
        "随机播放",
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvMusicList)
        val countTV = view.findViewById<TextView>(R.id.tv_count)
        val typeIV = view.findViewById<ImageView>(R.id.iv_list_type)
        val typeTV = view.findViewById<TextView>(R.id.tv_list_type)
        val mode = PlayerManager.binder.getPlayMode()
        typeIV.setImageResource(iconList[mode])
        typeTV.text = textList[mode]


        rv.layoutManager = LinearLayoutManager(context)

        adapter = MusicListAdapter({ pos ->
            // 点击播放回调
            PlayerManager.binder.switchAndPlay(pos)
        }, { pos ->
            // 点击删除回调
            PlayerManager.binder.remove(pos)
            val list = PlayerManager.binder.getPlayList()
            adapter.submitList(list)
            countTV.text = "${list.size}"
        })
        adapter.submitList(PlayerManager.binder.getPlayList())
        countTV.text = "${PlayerManager.binder.getPlayListSize()}"
        adapter.setPlayingId(PlayerManager.binder.getCurrentMusic().id)
        rv.adapter = adapter

        // 监听音乐
        PlayerManager.binder.addOnPreparedListener { music ->
            adapter.setPlayingId(music.id)
        }
        PlayerManager.binder.addOnPlayModeChangedListener { playMode ->
            typeIV.setImageResource(iconList[playMode])
            typeTV.text = textList[playMode]
        }

        view.findViewById<LinearLayout>(R.id.custom_button).setOnClickListener {
            PlayerManager.binder.switchPlayMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}