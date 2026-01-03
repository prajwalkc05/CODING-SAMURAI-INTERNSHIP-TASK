package com.example.firebasechatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter(
    private val userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvUserAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        
        // Prefer name, fallback to email prefix
        val displayName = if (!currentUser.name.isNullOrEmpty()) currentUser.name else currentUser.email?.substringBefore("@")
        
        holder.tvName.text = displayName
        holder.tvEmail.text = currentUser.email

        // Avatar: First letter
        if (!displayName.isNullOrEmpty()) {
           holder.tvAvatar.text = displayName[0].toString().uppercase()
        }

        holder.itemView.setOnClickListener {
            onUserClick(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}