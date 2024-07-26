package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.itemIconTintList = null // Remove icon tint list

        // Handle navigation item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.profileFragment -> {
                    true
                }
                R.id.bmiFragment -> {
                    startActivity(Intent(this, BMICalculatorActivity::class.java))
                    true
                }
                R.id.rewardsFragment -> {
                    if (currentUser != null) {
                        startActivity(Intent(this, RewardsActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please log in to view rewards", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        // Get the gymId from the intent
        val gymId = intent.getStringExtra("gymId")
        Log.d("ProfileActivity", "gymId: $gymId, currentUser: ${currentUser?.uid}")

        if (currentUser != null && gymId != null) {
            fetchUserProfile(currentUser.uid, gymId)
        } else {
            handleUserNotLoggedIn(currentUser, gymId)
        }

        // Logout button functionality
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val logoutIntent = Intent(this, InitialLoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
            finish()
        }
    }

    private fun fetchUserProfile(userId: String, gymId: String) {
        db.collection("Gym")
            .document(gymId)
            .collection("Members")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("First Name") ?: "No First Name"
                    val lastName = document.getString("Last Name") ?: "No Last Name"
                    val points = document.getDouble("Points") ?: 0.0
                    val email = document.getString("Email") ?: "No Email"

                    binding.firstNameTextView.text = "First Name: $firstName"
                    binding.lastNameTextView.text = "Last Name: $lastName"
                    binding.emailTextView.text = "Email: $email"
                    binding.pointsTextView.text = "Points: $points"
                } else {
                    Log.e("ProfileActivity", "Document does not exist or is null")
                    handleDocumentNotExist()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Error getting user details: ", exception)
                handleFetchError()
            }
    }

    private fun handleUserNotLoggedIn(currentUser: FirebaseUser?, gymId: String?) {
        if (currentUser == null) {
            Log.e("ProfileActivity", "User is not logged in")
        }
        if (gymId == null) {
            Log.e("ProfileActivity", "gymId is null")
        }
        binding.firstNameTextView.text = "First Name: Not Logged In"
        binding.lastNameTextView.text = "Last Name: Not Logged In"
        binding.emailTextView.text = "Email: Not Logged In"
        binding.pointsTextView.text = "Points: N/A"
    }

    private fun handleDocumentNotExist() {
        binding.firstNameTextView.text = "First Name: N/A"
        binding.lastNameTextView.text = "Last Name: N/A"
        binding.emailTextView.text = "Email: N/A"
        binding.pointsTextView.text = "Points: 0"
    }

    private fun handleFetchError() {
        binding.firstNameTextView.text = "First Name: Error"
        binding.lastNameTextView.text = "Last Name: Error"
        binding.emailTextView.text = "Email: Error"
        binding.pointsTextView.text = "Points: Error"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}
