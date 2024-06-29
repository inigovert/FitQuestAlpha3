package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
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
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        startActivity(Intent(this, RewardsActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please log in to view rewards", Toast.LENGTH_SHORT).show()
                        // Redirect to login page if necessary
                    }
                    true
                }
                else -> false
            }
        }

        if (currentUser != null) {
            // Fetch additional user information from Firestore
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "No First Name"
                        val lastName = document.getString("lastName") ?: "No Last Name"
                        val points = document.getDouble("points") ?: 0.0

                        binding.firstNameTextView.text = "First Name: $firstName"
                        binding.lastNameTextView.text = "Last Name: $lastName"
                        binding.emailTextView.text = "Email: ${currentUser.email}"
                        binding.pointsTextView.text = "Points: $points"
                    } else {
                        binding.firstNameTextView.text = "First Name: N/A"
                        binding.lastNameTextView.text = "Last Name: N/A"
                        binding.emailTextView.text = "Email: ${currentUser.email}"
                        binding.pointsTextView.text = "Points: 0"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileActivity", "Error getting user details: ", exception)
                    binding.firstNameTextView.text = "First Name: Error"
                    binding.lastNameTextView.text = "Last Name: Error"
                    binding.emailTextView.text = "Email: Error"
                    binding.pointsTextView.text = "Points: Error"
                }
        } else {
            // Handle case where user is not logged in (e.g., redirect to login screen)
            binding.firstNameTextView.text = "First Name: Not Logged In"
            binding.lastNameTextView.text = "Last Name: Not Logged In"
            binding.emailTextView.text = "Email: Not Logged In"
            binding.pointsTextView.text = "Points: N/A"
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

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}
