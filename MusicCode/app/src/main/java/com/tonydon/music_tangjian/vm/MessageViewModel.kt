package com.tonydon.music_tangjian.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tonydon.music_tangjian.db.entity.MessageEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private var conversationId = -1L
//    val historyMessages = dao.getForConversation(conversationId)

    val streamMessage: MutableStateFlow<MessageEntity?> = MutableStateFlow(null)


    fun streamResponse() {
        streamMessage.value =
            MessageEntity(content = "", isUser = false, conversationId = conversationId)
        viewModelScope.launch {
            repeat(100) {
                delay(100)
                streamMessage.update { msg ->
                    msg?.copy(content = msg.content + "0123456789".random())
                }
            }
        }
    }
}