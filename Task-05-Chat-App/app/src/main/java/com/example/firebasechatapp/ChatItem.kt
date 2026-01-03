package com.example.firebasechatapp

data class ChatItem(
    val chatId: String,
    val otherUserEmail: String,
    val lastMessage: String,
    val lastTime: Long = 0
)