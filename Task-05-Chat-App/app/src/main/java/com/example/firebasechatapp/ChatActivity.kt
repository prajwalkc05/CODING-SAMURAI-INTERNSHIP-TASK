package com.example.firebasechatapp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    // UI Components
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton
    private lateinit var progressBar: ProgressBar

    // Data
    private lateinit var messageList: ArrayList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var mDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Chat Room Info
    private var receiverUid: String? = null
    private var receiverEmail: String? = null
    private var chatRoomId: String? = null

    // Action Mode
    private var actionMode: ActionMode? = null

    // Forwarding
    private var textToForward: String? = null

    // Image Upload
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebase(it)
        }
    }

    // Forward Result Launcher
    private val forwardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
             val forwardUid = result.data?.getStringExtra("forward_to_uid")
             val forwardEmail = result.data?.getStringExtra("forward_to_email")
             
             if (textToForward != null && forwardUid != null) {
                 Toast.makeText(this, "Forwarding to $forwardEmail not fully implemented in this context", Toast.LENGTH_SHORT).show()
                 textToForward = null
             }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ✅ Step 5: Enable Back Button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ✅ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ Get Receiver Info from Intent
        receiverUid = intent.getStringExtra("receiverId")
        receiverEmail = intent.getStringExtra("email")

        // Set Title
        supportActionBar?.title = receiverEmail ?: "Chat"

        // ✅ Determine Chat Room ID (Must be consistent)
        val senderUid = currentUser.uid
        if (receiverUid != null) {
            // Sort UIDs to ensure same Room ID for both users
            chatRoomId = if (senderUid < receiverUid!!) {
                "${senderUid}_${receiverUid}"
            } else {
                "${receiverUid}_${senderUid}"
            }
        } else {
            // Fallback for global or error
            chatRoomId = "global_chat" 
        }

        // ✅ Initialize Database Reference to Specific Room
        mDbRef = FirebaseDatabase.getInstance().reference

        // Setup UI
        messageRecyclerView = findViewById(R.id.recyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        progressBar = findViewById(R.id.progressBar)

        messageList = ArrayList()
        adapter = ChatAdapter(messageList) { selectedCount ->
            if (selectedCount > 0) {
                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback)
                }
                actionMode?.title = "$selectedCount Selected"
                actionMode?.invalidate()
            } else {
                actionMode?.finish()
            }
        }

        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        // ✅ READ MESSAGES (Real-time) from chats -> chatRoomId -> messages
        mDbRef.child("chats").child(chatRoomId!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                            
                            // Mark as seen
                            if (message.senderId != auth.currentUser?.uid && message.status != "seen") {
                                 val map = HashMap<String, Any>()
                                 map["status"] = "seen"
                                 postSnapshot.ref.updateChildren(map)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    
                    if (messageList.isNotEmpty() && actionMode == null) {
                        messageRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, null)
            }
        }

        btnAttach.setOnClickListener {
             pickImageLauncher.launch("image/*")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.context_menu_chat, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_copy -> {
                    copySelectedMessages()
                    mode.finish() 
                    true
                }
                R.id.action_delete -> {
                    showDeleteBottomSheet()
                    // Keep Action Mode open until user selects an option
                    true
                }
                R.id.action_forward -> {
                    forwardSelectedMessages()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    private fun showDeleteBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_delete, null)
        bottomSheetDialog.setContentView(view)

        val tvDeleteForMe = view.findViewById<TextView>(R.id.tvDeleteForMe)
        val tvDeleteForEveryone = view.findViewById<TextView>(R.id.tvDeleteForEveryone)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)

        // Check if all selected messages are sent by me
        val currentUserId = auth.currentUser?.uid
        val selected = adapter.selectedMessages
        val allSentByMe = selected.all { it.senderId == currentUserId }

        if (!allSentByMe) {
            tvDeleteForEveryone.visibility = View.GONE
        }

        tvDeleteForMe.setOnClickListener {
            deleteForMe()
            bottomSheetDialog.dismiss()
            actionMode?.finish()
        }

        tvDeleteForEveryone.setOnClickListener {
            deleteForEveryone()
            bottomSheetDialog.dismiss()
            actionMode?.finish()
        }

        tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun deleteForMe() {
        val currentUserId = auth.currentUser?.uid ?: return
        val selected = ArrayList(adapter.selectedMessages) // Copy list

        for (msg in selected) {
            // ✅ Add current user ID to deletedFor map (set to true)
            // Path: /chats/{chatRoomId}/messages/{messageId}/deletedFor/{currentUserId}
            val dbRef = mDbRef.child("chats").child(chatRoomId!!).child("messages")
                .child(msg.messageId!!).child("deletedFor").child(currentUserId)
            
            dbRef.setValue(true)
        }
        Toast.makeText(this, "Messages deleted for me", Toast.LENGTH_SHORT).show()
    }

    private fun deleteForEveryone() {
        val selected = ArrayList(adapter.selectedMessages) 
        val currentUserId = auth.currentUser?.uid ?: return

        for (msg in selected) {
             if (msg.senderId == currentUserId) {
                 val dbRef = mDbRef.child("chats").child(chatRoomId!!).child("messages").child(msg.messageId!!)
                 val map = HashMap<String, Any?>()
                 // ✅ Set deletedForEveryone = true and clear text/image
                 map["messageText"] = ""
                 map["imageUrl"] = null
                 map["deletedForEveryone"] = true
                 dbRef.updateChildren(map)
             }
        }
        Toast.makeText(this, "Messages deleted for everyone", Toast.LENGTH_SHORT).show()
    }

    private fun copySelectedMessages() {
        val selected = adapter.selectedMessages
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val sb = StringBuilder()
        
        selected.sortBy { it.timestamp }
        
        for (msg in selected) {
            if (!msg.messageText.isNullOrEmpty() && !msg.deletedForEveryone) {
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(msg.messageText)
            }
        }

        if (sb.isNotEmpty()) {
            val clip = ClipData.newPlainText("Chat Messages", sb.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Messages copied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun forwardSelectedMessages() {
        val selected = adapter.selectedMessages
        val textMessages = selected.filter { !it.messageText.isNullOrEmpty() && !it.deletedForEveryone }.sortedBy { it.timestamp }
        
        if (textMessages.isEmpty()) {
            Toast.makeText(this, "No text messages to forward", Toast.LENGTH_SHORT).show()
            return
        }
        
        val sb = StringBuilder()
        for (msg in textMessages) {
             if (sb.isNotEmpty()) sb.append("\n")
             sb.append(msg.messageText)
        }
        
        textToForward = sb.toString()
        val intent = Intent(this, ContactsActivity::class.java)
        intent.putExtra("forward_text", textToForward)
        forwardLauncher.launch(intent)
    }

    private fun sendMessage(text: String?, imageUrl: String?, isForwarded: Boolean = false) {
        val senderUid = auth.currentUser?.uid ?: return
        val senderEmail = auth.currentUser?.email ?: "Anonymous"

        val chatRef = mDbRef.child("chats").child(chatRoomId!!).child("messages")
        val messageRef = chatRef.push()
        val messageId = messageRef.key ?: UUID.randomUUID().toString()
        
        val finalText = if (isForwarded && text != null) "Forwarded: $text" else text

        // ✅ Initialize with defaults including deletedForEveryone = false
        val messageObject = Message(
            messageId = messageId,
            senderId = senderUid,
            receiverId = receiverUid, 
            senderEmail = senderEmail,
            messageText = finalText,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            status = "sent",
            deletedForEveryone = false
        )

        messageRef.setValue(messageObject)
            .addOnSuccessListener {
                etMessage.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        progressBar.visibility = View.VISIBLE
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child("chat_images/$fileName")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    progressBar.visibility = View.GONE
                    sendMessage(null, uri.toString())
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}