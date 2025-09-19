package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.chat.MessageAdapter
import com.tonydon.music_tangjian.vm.MessageViewModel
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    lateinit var messageVM: MessageViewModel
    val messageAdapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 Activity 作用域的共享 ViewModel
        messageVM = ViewModelProvider(requireActivity())[MessageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.chatRecycler)
        val sendIB = view.findViewById<ImageView>(R.id.iv_chat_send)
        val inputET = view.findViewById<EditText>(R.id.et_chat_input)
        val analyzeBtn = view.findViewById<Button>(R.id.btn_analyze_music)

        // 对话消息的 Adapter
        rv.adapter = messageAdapter
        rv.layoutManager = LinearLayoutManager(context)

        // 用户发送消息
        sendIB.setOnClickListener {
            val content = inputET.text.trim()
            if (content.isNotEmpty()) {
                messageVM.sendMessage(content.toString())
                analyzeBtn.visibility = View.GONE
            }
            inputET.text.clear()
        }

        // 点击分析音乐喜好
        analyzeBtn.setOnClickListener {
            messageVM.analyze()
            analyzeBtn.visibility = View.GONE
        }

        // 监听消息列表的变化
        lifecycleScope.launch {
            launch {
                messageVM.messageList.collect { list ->
                    messageAdapter.submitList(list) {
                        if (list.isNotEmpty()) {
                            rv.smoothScrollToPosition(list.size - 1)
                        }
                    }
                }
            }
            launch {
                messageVM.conversationId.collect { id ->
                    // 新对话展示
                    if (id == -1L) {
                        analyzeBtn.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}