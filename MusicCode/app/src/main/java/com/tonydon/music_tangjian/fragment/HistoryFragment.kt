package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonydon.music_tangjian.DeepseekActivity
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.chat.ConversationAdapter
import com.tonydon.music_tangjian.vm.MessageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HistoryFragment : Fragment() {

    lateinit var messageVM: MessageViewModel

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
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as DeepseekActivity
        // 对话列表
        val rv = view.findViewById<RecyclerView>(R.id.left_recycler_view)
        val adapter = ConversationAdapter(
            onDelete = {

            },
            onSelected = { id ->
                messageVM.switchConversation(id)
                Log.d("tag_tj", "id = $id")
                activity.viewPager2.currentItem = 0
            }
        )
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context)

        // 打开新对话
        val newBtn = view.findViewById<Button>(R.id.btn_frag_new_conv)
        newBtn.setOnClickListener {
            messageVM.startNewConversation()
            activity.viewPager2.currentItem = 0
        }

        // 获取所有对话
        lifecycleScope.launch {
            messageVM.conversationDao.getAllFlow().collect { list ->
                adapter.submitList(list)
            }
        }
    }

}