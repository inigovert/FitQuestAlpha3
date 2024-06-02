package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // ... (Initialize UI elements) ...
        val emailInput = findViewById<EditText>(R.id.emailTextInput)
        val passwordInput = findViewById<EditText>(R.id.passwordTextInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        auth =  Firebase.auth

        loginButton.setOnClickListener {
            // ... (Input collection) ...
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Success! Login complete
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the main app screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Login failed, display an error message
                    Toast.makeText(this, "Login Failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}