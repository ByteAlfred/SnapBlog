package com.example.snapblog

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var signupRedirectText: TextView
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail = findViewById(R.id.editTextEmail)
        loginPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        signupRedirectText = findViewById(R.id.textViewSignUp)
        forgotPassword = findViewById(R.id.textViewForgotPassword)

        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            if (isValidEmail(email) && isValidPassword(pass)) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUp::class.java))
        }

        forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return !TextUtils.isEmpty(password) && password.length >= 6
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this@LoginActivity)
        val dialogView = layoutInflater.inflate(R.layout.forgot_password, null)
        val emailBox = dialogView.findViewById<EditText>(R.id.et_emailForgotPass)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<View>(R.id.btn_send_reset_email).setOnClickListener {
            val userEmail = emailBox.text.toString()

            if (isValidEmail(userEmail)) {
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Check your email", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@LoginActivity, "Unable to send, failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "Enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
    }
}
