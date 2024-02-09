package com.example.snapblog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapblog.Adapter.ProfileBlogAdapter
import com.example.snapblog.Models.Blog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var userImage: ImageView
    private lateinit var editProfileButton: ImageButton
    private lateinit var userNameTextView: TextView
    private lateinit var userCardView: CardView
    private lateinit var myBlogsTextView: TextView
    private lateinit var userBlogsRecyclerView: RecyclerView



    private lateinit var profileBlogAdapter: ProfileBlogAdapter
    private val userBlogsList: MutableList<Blog> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        userImage = findViewById(R.id.UserImage)
        editProfileButton = findViewById(R.id.editProfile)
        userNameTextView = findViewById(R.id.textViewUserName)
        userCardView = findViewById(R.id.userCardView)
        myBlogsTextView = findViewById(R.id.textViewMyBlogs)
        userBlogsRecyclerView = findViewById(R.id.userBlogsRecyclerView)

        setupRecyclerView()
        loadUserProfile()
        loadUserBlogs()


        findViewById<ImageView>(R.id.backButton).setOnClickListener {

            startActivity(Intent(this@AccountActivity, MainActivity::class.java))

        }
        findViewById<ImageButton>(R.id.editProfile).setOnClickListener {

            startActivity(Intent(this@AccountActivity, ProfileActivity::class.java))

        }
        findViewById<Button>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // After signing out, you might want to navigate to the login or another relevant activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }

    }

    private fun setupRecyclerView() {
        userBlogsRecyclerView.layoutManager = LinearLayoutManager(this)
        profileBlogAdapter = ProfileBlogAdapter(userBlogsList)
        userBlogsRecyclerView.adapter = profileBlogAdapter
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = databaseReference.child("users").child(userId)

            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Retrieve user details
                        val userName = snapshot.child("userName").getValue(String::class.java)
                        val profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)

                        // Display user details
                        userNameTextView.text = userName

                        // Load and display the profile picture using Glide
                        Glide.with(this@AccountActivity)
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(userImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }

    private fun loadUserBlogs() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val blogsReference = databaseReference.child("blogPosts")

            blogsReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Clear the existing list
                        userBlogsList.clear()

                        // Iterate through each blog post
                        for (blogSnapshot in snapshot.children) {
                            val blog = blogSnapshot.getValue(Blog::class.java)

                            // Check if the blog belongs to the current user
                            if (blog?.userId == userId) {
                                // Add the blog to the user's blog list
                                userBlogsList.add(blog)
                            }
                        }

                        // Update the adapter
                        profileBlogAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }
}
