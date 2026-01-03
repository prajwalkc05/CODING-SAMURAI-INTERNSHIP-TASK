package com.example.firebasechatapp

data class Message(
    val messageId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val senderEmail: String? = null,
    val messageText: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = 0,
    var status: String = "sent", // sent, delivered, seen
    val deletedForEveryone: Boolean = false,
    val deletedFor: HashMap<String, Boolean> = HashMap()
)