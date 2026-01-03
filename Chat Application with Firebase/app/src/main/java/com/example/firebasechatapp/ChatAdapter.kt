package com.example.firebasechatapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val messageList: ArrayList<Message>,
    private val onSelectionChanged: (Int) -> Unit // Callback to notify Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // ✅ Multi-Select Feature
    val selectedMessages = ArrayList<Message>()
    var isSelectionMode = false

    fun toggleSelection(message: Message) {
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message)
        } else {
            selectedMessages.add(message)
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedMessages.size)
    }

    fun clearSelection() {
        selectedMessages.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    // ✅ Helper to format timestamp
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // ViewHolder for Sent Messages
    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val timeText: TextView = itemView.findViewById(R.id.tvTime)
        val statusIcon: ImageView = itemView.findViewById(R.id.ivStatus)
        val imageMessage: ImageView = itemView.findViewById(R.id.ivMessage)
        val container: LinearLayout = itemView.findViewById(R.id.llMessageContainer)
        val bubble: LinearLayout = itemView.findViewById(R.id.llBubble)
    }

    // ViewHolder for Received Messages
    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val senderName: TextView = itemView.findViewById(R.id.tvSender)
        val timeText: TextView = itemView.findViewById(R.id.tvTime)
        val imageMessage: ImageView = itemView.findViewById(R.id.ivMessage)
        val container: LinearLayout = itemView.findViewById(R.id.llMessageContainer)
        val bubble: LinearLayout = itemView.findViewById(R.id.llBubble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        
        // Check if message is deleted for current user
        if (currentUserId != null && currentMessage.deletedFor.containsKey(currentUserId)) {
             // Hide message completely
             holder.itemView.visibility = View.GONE
             holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
             return
        } else {
             holder.itemView.visibility = View.VISIBLE
             holder.itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        val isSelected = selectedMessages.contains(currentMessage)

        if (holder.javaClass == SentViewHolder::class.java) {
            // Bind Sent Message
            val viewHolder = holder as SentViewHolder
            
            if (currentMessage.deletedForEveryone) {
                 viewHolder.imageMessage.visibility = View.GONE
                 viewHolder.sentMessage.visibility = View.VISIBLE
                 viewHolder.sentMessage.text = "🚫 This message was deleted"
                 viewHolder.sentMessage.setTypeface(null, Typeface.ITALIC)
                 viewHolder.sentMessage.setTextColor(Color.GRAY)
            } else {
                viewHolder.sentMessage.setTypeface(null, Typeface.NORMAL)
                viewHolder.sentMessage.setTextColor(Color.BLACK)
                
                // Handle Text vs Image
                if (currentMessage.imageUrl != null) {
                    viewHolder.imageMessage.visibility = View.VISIBLE
                    viewHolder.sentMessage.visibility = View.GONE
                    Glide.with(viewHolder.itemView.context)
                        .load(currentMessage.imageUrl)
                        .into(viewHolder.imageMessage)
                } else {
                    viewHolder.imageMessage.visibility = View.GONE
                    viewHolder.sentMessage.visibility = View.VISIBLE
                    viewHolder.sentMessage.text = currentMessage.messageText
                }
            }

            viewHolder.timeText.text = formatTime(currentMessage.timestamp)

            // Status Ticks Logic
            when (currentMessage.status) {
                "sent" -> viewHolder.statusIcon.setImageResource(R.drawable.ic_check_sent)
                "delivered" -> viewHolder.statusIcon.setImageResource(R.drawable.ic_check_delivered)
                "seen" -> viewHolder.statusIcon.setImageResource(R.drawable.ic_check_seen)
            }

            // ✅ Highlight Background if Selected
            if (isSelected) {
                viewHolder.container.setBackgroundColor(Color.parseColor("#B3E5FC")) // Light Blue
            } else {
                viewHolder.container.setBackgroundColor(Color.TRANSPARENT)
            }

            // Click Listeners
            viewHolder.itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(currentMessage)
                }
            }

            viewHolder.itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(currentMessage)
                }
                true
            }

        } else {
            // Bind Received Message
            val viewHolder = holder as ReceivedViewHolder
            
            if (currentMessage.deletedForEveryone) {
                 viewHolder.imageMessage.visibility = View.GONE
                 viewHolder.receivedMessage.visibility = View.VISIBLE
                 viewHolder.receivedMessage.text = "🚫 This message was deleted"
                 viewHolder.receivedMessage.setTypeface(null, Typeface.ITALIC)
                 viewHolder.receivedMessage.setTextColor(Color.GRAY)
            } else {
                 viewHolder.receivedMessage.setTypeface(null, Typeface.NORMAL)
                 viewHolder.receivedMessage.setTextColor(Color.BLACK)
                 
                 // Handle Text vs Image
                if (currentMessage.imageUrl != null) {
                    viewHolder.imageMessage.visibility = View.VISIBLE
                    viewHolder.receivedMessage.visibility = View.GONE
                    Glide.with(viewHolder.itemView.context)
                        .load(currentMessage.imageUrl)
                        .into(viewHolder.imageMessage)
                } else {
                    viewHolder.imageMessage.visibility = View.GONE
                    viewHolder.receivedMessage.visibility = View.VISIBLE
                    viewHolder.receivedMessage.text = currentMessage.messageText
                }
            }
            
            viewHolder.senderName.text = currentMessage.senderEmail
            viewHolder.timeText.text = formatTime(currentMessage.timestamp)

            // ✅ Highlight Background if Selected
            if (isSelected) {
                viewHolder.container.setBackgroundColor(Color.parseColor("#B3E5FC")) // Light Blue
            } else {
                viewHolder.container.setBackgroundColor(Color.TRANSPARENT)
            }

            // Click Listeners
             viewHolder.itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(currentMessage)
                }
            }

            viewHolder.itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(currentMessage)
                }
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (currentMessage.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}