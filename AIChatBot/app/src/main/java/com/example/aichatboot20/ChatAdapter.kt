package com.example.aichatboot20

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messageList: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_message, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bot_message, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = messageList[position]
        if (holder is UserViewHolder) {
            holder.userMsgTv.text = chatMessage.message
        } else if (holder is BotViewHolder) {
            holder.botMsgTv.text = formatMarkdownToSpanned(chatMessage.message)
        }
    }

    override fun getItemCount(): Int = messageList.size

    fun addMessage(message: ChatMessage) {
        messageList.add(message)
        notifyItemInserted(messageList.size - 1)
    }

    fun clearMessages() {
        val size = messageList.size
        messageList.clear()
        notifyItemRangeRemoved(0, size)
    }

    private fun formatMarkdownToSpanned(raw: String): CharSequence {
        return try {
            var formatted = raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace(Regex("(?m)^\\*\\s+"), "• ")
                .replace(Regex("\\*\\*([^*]+)\\*\\*"), "<b>$1</b>")
                .replace(Regex("\\*([^*]+)\\*"), "<i>$1</i>")
                .replace(Regex("`([^`]+)`"), "<tt>$1</tt>")
                .replace("\n", "<br/>")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(formatted)
            }
        } catch (e: Exception) {
            raw
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userMsgTv: TextView = itemView.findViewById(R.id.userMessageTextView)
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val botMsgTv: TextView = itemView.findViewById(R.id.botMessageTextView)
    }
}
