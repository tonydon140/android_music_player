package com.tonydon.music_tangjian.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.db.AppDatabase
import com.tonydon.music_tangjian.vm.MessageViewModel
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        val messageVM = ViewModelProvider(this)[MessageViewModel::class.java]
        lifecycleScope.launch {
            messageVM.streamMessage.collect { message ->
                if (message != null) {

                }
            }
        }
//        lifecycleScope.launchWhenStarted {
//            repeatOnLifecycle()
//            combine(
//                viewModel.historyMessages.asFlow(),
//                viewModel.streamingMessage
//            ) { history, streaming ->
//                if (streaming != null) history + streaming else history
//            }.collect { fullList ->
//                adapter.submitList(fullList)
//                recyclerView.scrollToPosition(fullList.size - 1)
//            }
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

}