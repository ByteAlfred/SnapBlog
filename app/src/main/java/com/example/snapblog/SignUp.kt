package com.example.snapblog

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var signUpButton: Button
    private lateinit var signInRedirectTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailEditText = findViewById(R.id.editTextEmailSignUp)
        passwordEditText = findViewById(R.id.editTextPasswordSignUp)
        fullNameEditText = findViewById(R.id.editTextFullNameSignUp)
        ageEditText = findViewById(R.id.editTextAgeSignUp)
        genderSpinner = findViewById(R.id.spinnerGenderSignUp)
        signUpButton = findViewById(R.id.buttonSignUp)
        signInRedirectTextView = findViewById(R.id.textViewSignInSignUp)

        auth = FirebaseAuth.getInstance()

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val gender = genderSpinner.selectedItem.toString()

            if (isValidEmail(email) && isValidPassword(password) && isValidFullName(fullName) && isValidAge(age)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign up success, navigate to your main activity or do additional actions
                            Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        signInRedirectTextView.setOnClickListener {
            // Redirect to the Sign In page
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return !TextUtils.isEmpty(password) && password.length >= 6
    }

    private fun isValidFullName(fullName: String): Boolean {
        return !TextUtils.isEmpty(fullName)
    }

    private fun isValidAge(age: String): Boolean {
        return !TextUtils.isEmpty(age)
    }
}
