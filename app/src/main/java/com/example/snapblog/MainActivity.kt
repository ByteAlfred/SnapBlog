package com.example.snapblog

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapblog.Adapter.BlogAdapter
import com.example.snapblog.Models.Blog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var topBlogsRecyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var editTextTopBlogs: EditText

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val blogList = mutableListOf<Blog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        topBlogsRecyclerView = findViewById(R.id.topBlogsRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        editTextTopBlogs = findViewById(R.id.topBlogsTextField)

        // Set up RecyclerView
        setupRecyclerView()

        findViewById<ImageButton>(R.id.navigationMenu).setOnClickListener {
            navigateToMenuPage()
        }

        findViewById<ImageButton>(R.id.accountButton).setOnClickListener {
            // Handle account button click
            navigateToAccountPage()
        }

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // Handle home button click
                    // You can implement your logic here
                    true
                }
                R.id.menu_search -> {
                    // Handle explore button click
                    startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                    true
                }
                R.id.menu_add -> {
                    // Handle notifications button click
                    startActivity(Intent(this@MainActivity, AddBlog::class.java))
                    true
                }
                R.id.menu_account -> {
                    // Handle account button click
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Fetch and display most recent blogs
        fetchRecentBlogs()
    }

    private fun navigateToMenuPage() {
        // Implement the logic to start the MenuActivity or any other activity you want
        // For example:
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAccountPage() {
        // Implement the logic to start the com.example.snapblog.com.example.snapblog.AccountActivity or any other activity you want
        // For example:
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        topBlogsRecyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = BlogAdapter(this, blogList) { selectedBlog ->
            val intent = Intent(this, BlogDetail::class.java)
           // intent.putExtra("BlogId", selectedBlog.blogId)
            startActivity(intent)
        }

        // Set item click listener for the RecyclerView
        blogAdapter.setOnItemClickListener { selectedBlog ->
            // Handle item click, navigate to BlogDetailActivity
            val intent = Intent(this, BlogDetail::class.java)
            intent.putExtra("BlogId", selectedBlog.blogId)
            startActivity(intent)
        }

        topBlogsRecyclerView.adapter = blogAdapter
    }

    private fun fetchRecentBlogs() {
        val query = databaseReference.child("blogPosts").limitToLast(15).orderByChild("date")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()

                for (postSnapshot in snapshot.children) {
                    val blog = postSnapshot.getValue(Blog::class.java)
                    if (blog != null) {
                        blogList.add(blog)
                    }
                }

                blogAdapter.setData(blogList.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
