package com.example.firebasechatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatsAdapter(
    private val chatList: ArrayList<ChatItem>,
    private val onChatClick: (String, String) -> Unit // chatId, otherUserEmail
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentChat = chatList[position]
        holder.tvEmail.text = currentChat.otherUserEmail
        holder.tvLastMessage.text = currentChat.lastMessage

        if (currentChat.otherUserEmail.isNotEmpty()) {
            holder.tvAvatar.text = currentChat.otherUserEmail[0].toString().uppercase()
        }

        holder.itemView.setOnClickListener {
            onChatClick(currentChat.chatId, currentChat.otherUserEmail)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}