package com.example.snapblog

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snapblog.Models.Blog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddBlog : AppCompatActivity() {

    // Initialize Firebase variables
    private val auth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val blogReference: DatabaseReference = database.getReference("blogPosts")

    private lateinit var blogTitleEditText: EditText
    private lateinit var blogDescriptionEditText: EditText
    private lateinit var datePickerEditText: EditText
    private lateinit var publishButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var imageViewBlogImage: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_blog)

        // Initialize UI elements
        blogTitleEditText = findViewById(R.id.editTextBlogTitle)
        blogDescriptionEditText = findViewById(R.id.editTextEditPost)
        datePickerEditText = findViewById(R.id.editTextDatePicker)
        publishButton = findViewById(R.id.buttonPublish)
        imageViewBlogImage = findViewById(R.id.imageViewBlogImage)
        bottomNavigationView = findViewById(R.id.bottomNavigation_View)


        // Set onClickListener for the back arrow
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // Set onClickListener for adding an image
        imageViewBlogImage.setOnClickListener {
            openImageChooser()
        }

        // Set onClickListener for the date picker
        datePickerEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // Set onClickListener for the publish button
        publishButton.setOnClickListener {
            validateAndSaveBlog()
        }
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // Handle home button click
                    startActivity(Intent(this@AddBlog, MainActivity::class.java))
                    true
                }
                R.id.menu_search -> {
                    // Handle explore button click
                    startActivity(Intent(this@AddBlog, SearchActivity::class.java))
                    true
                }
                R.id.menu_add -> {
                    // Handle notifications button click
                    true
                }
                R.id.menu_account -> {
                    // Handle account button click
                    startActivity(Intent(this@AddBlog, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

    }


    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageViewBlogImage.setImageURI(imageUri)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                // Validate the selected date here
                if (isValidDate(selectedDate)) {
                    datePickerEditText.setText(selectedDate)
                } else {
                    Toast.makeText(this, "Invalid date selected", Toast.LENGTH_SHORT).show()
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            // Attempt to parse the date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(date)
            true
        } catch (e: ParseException) {
            false
        }
    }


    private fun validateAndSaveBlog() {
        val title = blogTitleEditText.text.toString().trim()
        val description = blogDescriptionEditText.text.toString().trim()
        val date = datePickerEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
            // Handle validation errors
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate description length
        if (description.length > 500) {
            Toast.makeText(this, "Description must be less than 500 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Save the blog post to Realtime Database
        saveBlogToRealtimeDatabase(title, description, date)
    }




    private fun saveBlogToRealtimeDatabase(title: String, description: String, date: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Generate a unique blogId using UUID
            val blogId = UUID.randomUUID().toString()

            val blogPost = Blog(blogId, title, description, "", date, userId, likes = 0)

            // Use the generated blogId instead of the push().key
            blogReference.child(blogId).setValue(blogPost)
                .addOnSuccessListener {
                    // Upload image to storage and update imageUrl in Realtime Database (implement this logic)
                    uploadImageToStorage(blogId)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error publishing blog post", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun uploadImageToStorage(blogId: String) {
        // Check if imageUri is not null
        if (imageUri != null) {
            // Create a reference to "blog_images" in Firebase Storage
            val storageReference: StorageReference = FirebaseStorage.getInstance().getReference("blog_images")

            // Generate a unique name for the image
            val imageName = "${UUID.randomUUID()}.jpg"

            // Create a reference to the image file
            val imageRef: StorageReference = storageReference.child(imageName)

            // Upload the image to Firebase Storage
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update the 'imageUrl' field in the Realtime Database
                        updateImageUrlInDatabase(blogId, uri.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle the failure
                    // You may want to display an error message to the user
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If imageUri is null, update the 'imageUrl' field with an empty string in the Realtime Database
            updateImageUrlInDatabase(blogId, "")
        }
    }

    private fun updateImageUrlInDatabase(blogId: String, imageUrl: String) {
        // Create a reference to the blog post in the Realtime Database
        val blogPostRef = blogReference.child(blogId)

        // Update the 'imageUrl' field with the download URL
        blogPostRef.child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                // Image URL updated successfully
                Toast.makeText(this, "Blog post published successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@AddBlog, MainActivity::class.java))

                finish()
            }
            .addOnFailureListener {
                // Handle the failure
                // You may want to display an error message to the user
                Toast.makeText(this, "Error updating image URL in database", Toast.LENGTH_SHORT).show()
            }
    }

}
