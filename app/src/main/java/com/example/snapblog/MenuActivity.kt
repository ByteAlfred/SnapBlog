// MenuActivity.kt
package com.example.snapblog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Set click listeners for menu items
        findViewById<TextView>(R.id.textViewHome).setOnClickListener(this)
        findViewById<TextView>(R.id.textViewProfile).setOnClickListener(this)
        findViewById<TextView>(R.id.textViewSearch).setOnClickListener(this)
        findViewById<TextView>(R.id.textViewAddBlog).setOnClickListener(this)
        // Add more click listeners for additional menu items

        // Set click listener for the back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {

            startActivity(Intent(this@MenuActivity, MainActivity::class.java))

        }
    }

    override fun onClick(v: View) {
        // Handle menu item clicks
        when (v.id) {
            R.id.textViewHome -> startActivity(Intent(this, MainActivity::class.java))
            R.id.textViewProfile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.textViewSearch -> startActivity(Intent(this, SearchActivity::class.java))
            R.id.textViewAddBlog -> startActivity(Intent(this, AddBlog::class.java))
            // Add more cases for additional menu items
        }
    }
}
