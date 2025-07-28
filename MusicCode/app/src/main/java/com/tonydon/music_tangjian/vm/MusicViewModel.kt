package com.tonydon.music_tangjian.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicViewModel : ViewModel() {
//    val music = MutableStateFlow<MusicInfo>()
    val progressMs: StateFlow<Long> = MutableStateFlow(0L)

}