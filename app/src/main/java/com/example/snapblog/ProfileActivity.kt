package com.example.snapblog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePictureImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var secondNameEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    private var selectedImageUri: Uri? = null

    // Activity result launcher for selecting an image
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the selected image URI
            val data: Intent? = result.data
            selectedImageUri = data?.data

            // Display the selected image in the ImageView
            profilePictureImageView.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        profilePictureImageView = findViewById(R.id.profileImageView)
        firstNameEditText = findViewById(R.id.FirstNameEditText)
        secondNameEditText = findViewById(R.id.SecondNameEditText)
        userNameEditText = findViewById(R.id.userNameEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupBottomNavigationBar()

        // Set up the click listener to choose an image
        profilePictureImageView.setOnClickListener {
            selectImageLauncher.launch(getImageSelectionIntent())
        }

        saveProfileButton.setOnClickListener {
            saveProfileDetails()
        }

        // Load existing user details if available
        loadUserProfile()

        findViewById<ImageView>(R.id.backButton).setOnClickListener {

            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))

        }
    }

    private fun getImageSelectionIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    private fun saveProfileDetails() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            // Get user inputs
            val firstName = firstNameEditText.text.toString()
            val secondName = secondNameEditText.text.toString()
            val userName = userNameEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()

            // Save details to Firebase Database
            userReference.child("firstName").setValue(firstName)
            userReference.child("secondName").setValue(secondName)
            userReference.child("userName").setValue(userName)
            userReference.child("phoneNumber").setValue(phoneNumber)

            // Upload the profile picture if selected
            uploadProfilePicture(userId)

            // Display a success message (you can customize this)
            showToast("Profile details saved successfully!")

            // Example: navigate back to MainActivity
            finish()
        }
    }

    private fun uploadProfilePicture(userId: String) {
        if (selectedImageUri != null) {
            // Create a reference to the profile pictures folder
            val profilePicturesRef = storageReference.child("profile_pictures")

            // Create a reference to the specific user's profile picture
            val userProfilePictureRef = profilePicturesRef.child("$userId.jpg")

            // Upload the file to Firebase Storage
            userProfilePictureRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    // Get the download URL for the uploaded image
                    userProfilePictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Save the download URL to the user's profile in the database
                        FirebaseDatabase.getInstance().reference.child("users").child(userId).child("profilePictureUrl").setValue(downloadUri.toString())
                    }
                }
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            userReference.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Retrieve existing user details
                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                    val secondName = snapshot.child("secondName").getValue(String::class.java)
                    val userName = snapshot.child("userName").getValue(String::class.java)
                    val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java)
                    val profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)

                    // Display the retrieved details in the UI
                    firstNameEditText.setText(firstName)
                    secondNameEditText.setText(secondName)
                    userNameEditText.setText(userName)
                    phoneNumberEditText.setText(phoneNumber)

                    // Load and display the profile picture using Glide
                    Glide.with(this)
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.ic_user_placeholder) // Placeholder image while loading
                        .into(profilePictureImageView)
                }
            }
        }
    }

    private fun showToast(message: String) {
        // Display a toast message
    }

    private fun setupBottomNavigationBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    // Handle home button click
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                    true
                }
                R.id.menu_search -> {
                    // Handle explore button click
                    startActivity(Intent(this@ProfileActivity, SearchActivity::class.java))
                    true
                }
                R.id.menu_add -> {
                    // Handle notifications button click
                    startActivity(Intent(this@ProfileActivity, AddBlog::class.java))
                    true
                }
                R.id.menu_account -> {
                    // Handle account button click
                    true
                }
                else -> false
            }
        }
    }
}
