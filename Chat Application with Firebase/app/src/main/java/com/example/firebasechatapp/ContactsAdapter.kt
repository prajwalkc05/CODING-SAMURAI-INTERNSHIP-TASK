package com.example.firebasechatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    private val userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.tvEmail.text = currentUser.email

        // Set First Letter as Avatar
        if (!currentUser.email.isNullOrEmpty()) {
            holder.tvAvatar.text = currentUser.email[0].toString().uppercase()
        }

        holder.itemView.setOnClickListener {
            onUserClick(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}