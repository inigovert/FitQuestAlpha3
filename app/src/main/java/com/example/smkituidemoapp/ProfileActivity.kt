package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        db = FirebaseFirestore.getInstance()

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.itemIconTintList = null

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

        if (currentUser != null) {
            loadUserData(currentUser.email)
        } else {
            binding.firstNameTextView.text = "First Name: Not Logged In"
            binding.lastNameTextView.text = "Last Name: Not Logged In"
            binding.emailTextView.text = "Email: Not Logged In"
            binding.pointsTextView.text = "Points: N/A"
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val logoutIntent = Intent(this, InitialLoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
            finish()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(this, "Selected date: $date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData(email: String?) {
        if (email != null) {
            db.collectionGroup("Members")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.first()
                        val firstName = document.getString("First Name") ?: "No First Name"
                        val lastName = document.getString("Last Name") ?: "No Last Name"
                        val email = document.getString("Email") ?: "No Email"
                        val points = document.getDouble("Points") ?: 0.0

                        Log.d("ProfileActivity", "Retrieved data - FirstName: $firstName, LastName: $lastName, Email: $email, Points: $points")

                        binding.firstNameTextView.text = "First Name: $firstName"
                        binding.lastNameTextView.text = "Last Name: $lastName"
                        binding.emailTextView.text = "Email: $email"
                        binding.pointsTextView.text = "Points: $points"
                    } else {
                        Log.d("ProfileActivity", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileActivity", "Failed to retrieve user data: ", exception)
                }
        }
    }
}
