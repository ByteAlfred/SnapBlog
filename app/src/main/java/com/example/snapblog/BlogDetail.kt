package com.example.snapblog

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogDetail : AppCompatActivity() {

    private lateinit var blogImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var editButton: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var likeButton: ImageView
    private lateinit var likesCountTextView: TextView

    private lateinit var blogId: String
    private lateinit var userId: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_detail)

        // Initialize views
        blogImageView = findViewById(R.id.blogImageView)
        titleTextView = findViewById(R.id.titleTextView)
        dateTextView = findViewById(R.id.dateTextView)
        descriptionTextView = findViewById(R.id.descriptionView)
        editButton = findViewById(R.id.edit_Button)
        deleteButton = findViewById(R.id.delete_Button)
        likeButton = findViewById(R.id.like_Button)

        // Initialize Firebase
        // Assume your database reference is obtained appropriately, e.g., FirebaseDatabase.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Get the blogId from the intent
        blogId = intent.getStringExtra("blogId") ?: ""
        userId = auth.currentUser?.uid ?: ""

        // Fetch blog details from Firebase
        if (blogId.isNotEmpty()) {
            fetchBlogDetails(blogId)
        } else {
            // Handle the case where blogId is not provided
            // You may want to show an error message or navigate back to the previous activity
        }

        // Implement logic for buttons
        editButton.setOnClickListener { showEditDialog() }
        deleteButton.setOnClickListener { onDeleteButtonClick() }
        likeButton.setOnClickListener { onLikeButtonClick() }
    }

    private fun fetchBlogDetails(blogId: String) {
        // Assuming you have a "blogs" node in your database
        val blogRef = databaseReference.child("blogPosts").child(blogId)

        blogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Retrieve blog details here
                    val title = snapshot.child("title").value.toString()
                    val date = snapshot.child("date").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val imageUrl = snapshot.child("imageUrl").value.toString()

                    // Update views with fetched data
                    Glide.with(this@BlogDetail)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .into(blogImageView)

                    titleTextView.text = title
                    dateTextView.text = date
                    descriptionTextView.text = description

                    // Check if the current user liked the blog
                    checkIfUserLikedBlog(blogId)
                } else {
                    // Blog with the specified ID not found
                    // Handle this case, for example, display an error message
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun showEditDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_edit_description, null)

        val descriptionEditText: EditText = view.findViewById(R.id.editTextDescription)
        descriptionEditText.setText(descriptionTextView.text)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Edit Description")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                // Save the edited description to Firebase
                saveEditedDescription(descriptionEditText.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun saveEditedDescription(newDescription: String) {
        val blogRef = databaseReference.child("blogPosts").child(blogId)
        blogRef.child("description").setValue(newDescription)
            .addOnSuccessListener {
                // Update the UI with the new description
                descriptionTextView.text = newDescription
                showToast("Description updated successfully.")
            }
            .addOnFailureListener {
                // Handle the failure
                showToast("Failed to update description.")
            }
    }

    private fun onDeleteButtonClick() {
        // Check if the current user is the author of the blog
        val blogRef = databaseReference.child("blogPosts").child(blogId)
        blogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val authorId = snapshot.child("userId").value.toString()

                if (userId == authorId) {
                    // Current user is the author, allow deletion
                    deleteBlog()
                } else {
                    // Current user is not the author, show a message or handle it as needed
                    showToast("You are not authorized to delete this blog.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun deleteBlog() {
        val blogRef = databaseReference.child("blogPosts").child(blogId)
        blogRef.removeValue()
            .addOnSuccessListener {
                // Blog deleted successfully
                showToast("Blog deleted successfully.")
                finish() // Close the activity after deletion
            }
            .addOnFailureListener {
                // Handle the failure
                showToast("Failed to delete blog.")
            }
    }

    private fun onLikeButtonClick() {
        // Check if the user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showToast("Authentication required. Please log in to like or unlike blogs.")
            return
        }

        // Check if the user already liked the blog
        val likesRef = databaseReference.child("likes").child(blogId).child(currentUser.uid)
        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User already liked the blog, unlike it
                    likesRef.removeValue()
                    showToast("You unliked this blog.")
                } else {
                    // User has not liked the blog, like it
                    likesRef.setValue(true)
                    showToast("You liked this blog.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                showToast("Failed to check likes. Please try again.")
            }
        })
    }


    private fun checkIfUserLikedBlog(blogId: String) {
        // Check if the current user liked the blog
        val likesRef = databaseReference.child("likes").child(blogId).child(userId)
        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User liked the blog, update UI accordingly
                    likeButton.setImageResource(R.drawable.liked_icon)
                } else {
                    // User has not liked the blog
                    likeButton.setImageResource(R.drawable.like_icon)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
