package com.example.firebasechatapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var fabCamera: FloatingActionButton
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage

    private var imageUri: Uri? = null
    private var currentUser: User? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            ivProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        
        // ✅ Debug Log to check Auth Status
        val currentUserAuth = auth.currentUser
        if (currentUserAuth == null) {
            Log.d("AUTH", "NOT LOGGED IN")
            Toast.makeText(this, "Session expired, please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            Log.d("AUTH", "User ID: ${currentUserAuth.uid}")
        }

        dbRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance()

        ivProfile = findViewById(R.id.ivProfile)
        fabCamera = findViewById(R.id.fabCamera)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        loadUserProfile()

        fabCamera.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email
        etEmail.setText(email)

        progressBar.visibility = View.VISIBLE
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                if (currentUser != null) {
                    etName.setText(currentUser?.name)
                    
                    if (!currentUser?.profileImage.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity)
                            .load(currentUser?.profileImage)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfile)
                    }
                }
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfile() {
        // ✅ STEP 1: Check Auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = "Name required"
            return
        }

        progressBar.visibility = View.VISIBLE
        
        if (imageUri != null) {
            uploadImageAndSave(uid, name)
        } else {
            updateDatabase(uid, name, currentUser?.profileImage)
        }
    }

    private fun uploadImageAndSave(uid: String, name: String) {
        val ref = storageRef.reference.child("profile_images/$uid.jpg")
        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    updateDatabase(uid, name, uri.toString())
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDatabase(uid: String, name: String, profileImageUrl: String?) {
        val updates = HashMap<String, Any?>()
        updates["name"] = name
        updates["profileImage"] = profileImageUrl
        updates["email"] = auth.currentUser?.email
        updates["uid"] = uid

        // ✅ STEP 2 & 3: Correct update logic and path
        dbRef.child("users").child(uid).updateChildren(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}