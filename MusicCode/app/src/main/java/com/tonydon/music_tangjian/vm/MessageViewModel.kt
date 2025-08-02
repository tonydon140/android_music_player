package com.tonydon.music_tangjian.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tonydon.music_tangjian.data.Message
import com.tonydon.music_tangjian.db.AppDatabase
import com.tonydon.music_tangjian.db.entity.ConversationEntity
import com.tonydon.music_tangjian.db.entity.MessageEntity
import com.tonydon.music_tangjian.utils.HttpUtils
import com.tonydon.music_tangjian.utils.MessageCryptoUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min


class MessageViewModel(application: Application) : AndroidViewModel(application) {

    // 加载数据库
    private val db = AppDatabase.getInstance(application)
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()

    private var conversationId = -1L


    private val internalList = mutableListOf<Message>()
    val messageList = MutableStateFlow<List<Message>>(emptyList())

    /**
     * 用户发送消息
     */
    fun sendMessage(content: String) {
        // 插入用户的消息
        internalList.add(Message(isUser = true, content = content))
        messageList.value = internalList.toList()

        // 插入一条 AI 的空消息
        val message = Message(isUser = false, "")
        internalList.add(message)
        messageList.value = internalList.toList()

        // 请求 API，流式更新
        viewModelScope.launch(Dispatchers.IO) {
            // 如果是新对话，则创建 conversation
            if (conversationId == -1L) {
                conversationId =
                    conversationDao.insert(ConversationEntity(title = content.substring(0, min(10, content.length))))
            }

            // 插入用户消息
            val (encryptedContent, iv) = MessageCryptoUtil.encryptMessage(content) // 加密消息
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId,
                    content = encryptedContent,
                    iv = iv,
                    isUser = true
                )
            )

            // 请求 API，流式更新数据
            val builder = StringBuilder()
            HttpUtils.streamResponse(content) { responseText ->
                builder.append(responseText)
                internalList[internalList.lastIndex] = Message(isUser = false, builder.toString())
                messageList.value = internalList.toList()
            }

            // 请求完毕，写入数据库
            val (ecAI, ivAI) = MessageCryptoUtil.encryptMessage(builder.toString()) // 加密消息
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId,
                    content = ecAI,
                    iv = ivAI,
                    isUser = false
                )
            )
        }
    }

    /**
     * 初始化为“新对话”，但不创建数据库记录
     */
    fun startNewConversation() {
        conversationId = -1L
        messageList.value = emptyList()
    }


    /**
     * 切换到指定对话
     */
    fun switchConversation(id: Long) {
        conversationId = id
        loadMessages()
    }

    /**
     * 加载指定会话的所有消息
     */
    private fun loadMessages() {
        if (conversationId == -1L) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageDao.getForConversation(conversationId)
            val newList = messages.map { item ->
                val decrypted = MessageCryptoUtil.decryptMessage(item.content, item.iv)
                Message(item.isUser, decrypted)
            }
            messageList.value = newList
        }
    }
}