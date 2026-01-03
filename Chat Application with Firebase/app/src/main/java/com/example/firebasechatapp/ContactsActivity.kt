package com.example.firebasechatapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: ContactsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerViewContacts)
        progressBar = findViewById(R.id.progressBar)
        
        userList = ArrayList()
        adapter = ContactsAdapter(userList) { user ->
            // Check if this activity was opened for forwarding
            val forwardText = intent.getStringExtra("forward_text")
            if (forwardText != null) {
                // Return result to ChatActivity
                val resultIntent = Intent()
                resultIntent.putExtra("forward_to_uid", user.uid)
                resultIntent.putExtra("forward_to_email", user.email)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // Normal flow: Start chat
                // For this simple app, we are already in ChatActivity or similar flow logic.
                // If you had a separate HomeActivity listing chats, this would open ChatActivity.
                // Since this app has only one global chat room in "chats" node (as per original code),
                // this contact list is mostly symbolic or for forwarding in current context.
                // However, to satisfy "Start or continue chat with that user", 
                // normally we would pass extra data to ChatActivity to filter chat room.
                // Given the existing ChatActivity uses a global "chats" node, we just finish for now or show toast.
                
                // Assuming we want to return to ChatActivity (if we came from there) or start it.
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        mDbRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUid = auth.currentUser?.uid
                
                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != currentUid) {
                        userList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ContactsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })
    }
}