package com.tonydon.music_tangjian.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import com.tonydon.music_tangjian.data.Message
import com.tonydon.music_tangjian.db.AppDatabase
import com.tonydon.music_tangjian.db.entity.ConversationEntity
import com.tonydon.music_tangjian.db.entity.MessageEntity
import com.tonydon.music_tangjian.utils.ConfigUtils
import com.tonydon.music_tangjian.utils.HttpUtils
import com.tonydon.music_tangjian.utils.MessageCryptoUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min


class MessageViewModel(application: Application) : AndroidViewModel(application) {

    // 加载数据库
    private val db = AppDatabase.getInstance(application)
    val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()

    val conversationId = MutableStateFlow<Long>(-1L)
    val mmkv = MMKV.defaultMMKV()


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
            if (conversationId.value == -1L) {
                conversationId.value =
                    conversationDao.insert(
                        ConversationEntity(
                            title = content.substring(
                                0,
                                min(10, content.length)
                            )
                        )
                    )
            }

            // 插入用户消息
            val (encryptedContent, iv) = MessageCryptoUtil.encryptMessage(content) // 加密消息
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId.value,
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
                    conversationId = conversationId.value,
                    content = ecAI,
                    iv = ivAI,
                    isUser = false
                )
            )
        }
    }

    /**
     *
     */
    fun analyze() {
        val content = "分析我的音乐画像..."
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
            if (conversationId.value == -1L) {
                conversationId.value =
                    conversationDao.insert(
                        ConversationEntity(
                            title = content.substring(
                                0,
                                min(10, content.length)
                            )
                        )
                    )
            }

            // 插入用户消息
            val (encryptedContent, iv) = MessageCryptoUtil.encryptMessage(content) // 加密消息
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId.value,
                    content = encryptedContent,
                    iv = iv,
                    isUser = true
                )
            )

            // 构建提示词
            val favList = ConfigUtils.getAllFavoriteMusic()
            val prompt = buildString {
                append("请根据以下我收藏的音乐，分析我的音乐偏好画像，包括：")
                append("常见风格、情感倾向、节奏偏好等。\n")
                append("每首歌以如下格式提供：\n")
                append("《歌曲名》 - 歌手\n\n")
                favList.forEach {
                    append("《${it.musicName}》 - ${it.author}\n")
                }
                append("\n请用简洁准确的语言总结，不要列出所有歌曲，按照常见风格、情感倾向、节奏偏好类别直接输出属于我的类型，不用分析。")
            }
            Log.d("tag_tj", prompt)

            // 请求 API，流式更新数据
            val builder = StringBuilder()
            HttpUtils.streamResponse(prompt) { responseText ->
                builder.append(responseText)
                internalList[internalList.lastIndex] = Message(isUser = false, builder.toString())
                messageList.value = internalList.toList()
            }

            // 请求完毕，写入数据库
            val (ecAI, ivAI) = MessageCryptoUtil.encryptMessage(builder.toString()) // 加密消息
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId.value,
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
        conversationId.value = -1L
        internalList.clear()
        messageList.value = internalList.toList()
    }


    /**
     * 切换到指定对话
     */
    fun switchConversation(id: Long) {
        conversationId.value = id
        loadMessages()
    }

    /**
     * 加载指定会话的所有消息
     */
    private fun loadMessages() {
        if (conversationId.value == -1L) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageDao.getForConversation(conversationId.value)
            internalList.clear()
            internalList.addAll(messages.map { item ->
                val decrypted = MessageCryptoUtil.decryptMessage(item.content, item.iv)
                Message(item.isUser, decrypted)
            })
            messageList.value = internalList.toList()
        }
    }
}