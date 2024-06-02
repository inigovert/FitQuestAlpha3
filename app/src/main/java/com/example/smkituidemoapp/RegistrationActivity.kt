package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Initialize Firebase Authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = Firebase.auth // Get an instance of Firebase Authentication

        val emailInput = findViewById<EditText>(R.id.emailTextInput)
        val passwordInput = findViewById<EditText>(R.id.passwordTextInput)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Add basic input validation here (e.g., check if fields are empty)

            createAccount(email, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Success! Registration complete
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the main app screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() // Finish RegistrationActivity to prevent returning with back button

                } else {
                    // Registration failed, display an error message
                    Toast.makeText(this, "Registration Failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
