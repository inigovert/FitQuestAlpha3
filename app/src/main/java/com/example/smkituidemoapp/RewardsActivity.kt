package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityBmiCalculatorBinding
import com.example.smkituidemoapp.databinding.ActivityRewardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val pointsTextView = findViewById<TextView>(R.id.pointsTextView) // Assuming these IDs exist

        lateinit var binding: ActivityRewardsBinding

        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val bottomNavigationView = binding.bottomNavigation

        // Handle navigation item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true

                }

                R.id.profileFragment -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.rewardsFragment -> {
                    startActivity(Intent(this, RewardsActivity::class.java))
                    true
                }

                R.id.bmiFragment -> {
                    startActivity(Intent(this, BMICalculatorActivity::class.java))
                    true
                }

                else -> false
            }
        }

        if (currentUser != null) {
            nameTextView.text = currentUser.displayName ?: "No Name"

            // Fetch points from Firestore
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val points = document.getDouble("points") ?: 0.0
                        pointsTextView.text = "Points: $points"
                    } else {
                        pointsTextView.text = "Points: 0"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("RewardsActivity", "Error getting points: ", exception)
                    // Handle error, e.g., display an error message to the user
                }
        } else {
            // Handle case where user is not logged in (e.g., redirect to login screen)
            nameTextView.text = "Not Logged In"
            pointsTextView.text = "Points: N/A"
        }


    }
}
