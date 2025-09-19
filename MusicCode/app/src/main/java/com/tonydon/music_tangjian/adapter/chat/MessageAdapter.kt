package com.tonydon.music_tangjian.adapter.chat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.data.Message
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

class MessageAdapter : BaseDifferAdapter<Message, QuickViewHolder>(MessageDiff()) {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: Message?
    ) {
        if (item == null) {
            return
        }
//        val markwon = Markwon.builder(context)
//            .usePlugin(JLatexMathPlugin.create())
        val markwon = Markwon.create(context)
        val leftChatView = holder.getView<LinearLayout>(R.id.left_chat_view)
        val rightChatView = holder.getView<LinearLayout>(R.id.right_chat_view)
        val leftTV = holder.getView<TextView>(R.id.left_chat_text_view)
        val rightTV = holder.getView<TextView>(R.id.right_chat_text_view)
        if (item.isUser) {
            leftChatView.visibility = View.GONE
            rightChatView.visibility = View.VISIBLE
            rightTV.text = item.content
        } else {
            rightChatView.visibility = View.GONE
            leftChatView.visibility = View.VISIBLE
            markwon.setMarkdown(leftTV, item.content)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_chat, parent)
    }
}