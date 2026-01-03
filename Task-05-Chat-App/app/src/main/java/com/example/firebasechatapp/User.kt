package com.example.firebasechatapp

data class User(
    val uid: String? = null,
    val email: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val fcmToken: String? = null
)