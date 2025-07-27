package com.tonydon.music_tangjian.vm

import androidx.lifecycle.ViewModel
import com.tonydon.music_tangjian.service.MusicService

class MusicViewModel(
    val service: MusicService
) : ViewModel() {

}