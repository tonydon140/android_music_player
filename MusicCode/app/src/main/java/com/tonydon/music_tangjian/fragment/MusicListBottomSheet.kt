package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.MusicListAdapter
import com.tonydon.music_tangjian.service.PlayerManager
import kotlinx.coroutines.launch
import kotlin.math.log

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
        val mode = PlayerManager.playMode.value
        typeIV.setImageResource(iconList[mode])
        typeTV.text = textList[mode]

        adapter = MusicListAdapter({ id ->
            // 点击播放回调
            PlayerManager.switchAndPlay(id)
        }, { id ->
            // 点击删除回调
            Log.d("music_delete", "id = $id")
            PlayerManager.remove(id)
        })
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    PlayerManager.playMode.collect { mode ->
                        typeIV.setImageResource(iconList[mode])
                        typeTV.text = textList[mode]
                    }
                }
                launch {
                    PlayerManager.currentMusic.collect { music ->
                        if (music != null) {
                            adapter.setPlayingId(music.id)
                        }
                    }
                }
                launch {
                    PlayerManager.playlist.collect { playlist ->
                        adapter.submitList(playlist)
                        countTV.text = "${playlist.size}"
                        if (playlist.isEmpty()) {
                            dismiss()
                        }
                    }
                }
            }
        }

        view.findViewById<LinearLayout>(R.id.custom_button).setOnClickListener {
            PlayerManager.switchPlayMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}