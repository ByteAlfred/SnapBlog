package com.example.snapblog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapblog.Adapter.BlogAdapter
import com.example.snapblog.Models.Blog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


// Import statements
// ...

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up search button click listener
        searchButton.setOnClickListener {
            performSearch()
        }

        // Optional: Set up back button click listener
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // Optional: Set up bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // Handle home button click
                    startActivity(Intent(this@SearchActivity, MainActivity::class.java))
                    true
                }
                R.id.menu_search -> {
                    // Handle explore button click
                    true
                }
                R.id.menu_add -> {
                    // Handle notifications button click
                    startActivity(Intent(this@SearchActivity, AddBlog::class.java))
                    true
                }
                R.id.menu_account -> {
                    // Handle account button click
                    startActivity(Intent(this@SearchActivity, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = BlogAdapter(this, mutableListOf()) { selectedBlog ->
            // Handle item click, navigate to BlogDetailActivity
            val intent = Intent(this, BlogDetail::class.java)
           // intent.putExtra("blogId", selectedBlog.blogId)
            startActivity(intent)
        }
        searchResultsRecyclerView.adapter = blogAdapter
    }

    private fun performSearch() {
        // Get the user input
        val searchQuery = searchEditText.text.toString().trim()

        // Query Firebase Database for blogs with titles containing the search query
        val query = databaseReference.child("blogPosts")
            .orderByChild("title")
            .startAt(searchQuery)
            .endAt(searchQuery + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogList = mutableListOf<Blog>()

                for (postSnapshot in snapshot.children) {
                    val blog = postSnapshot.getValue(Blog::class.java)
                    if (blog != null) {
                        blogList.add(blog)
                    }
                }

                // Update the data in the BlogAdapter and notify data set changed
                blogAdapter.setData(blogList)
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(this@SearchActivity, "Error fetching search results", Toast.LENGTH_SHORT).show()
            }
        })
    }
}




