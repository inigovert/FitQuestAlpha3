package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smkituidemoapp.databinding.ActivityRewardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rewardsRecyclerView: RecyclerView // Declare RecyclerView variable

    private var userPoints: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        rewardsRecyclerView = binding.rewardsRecyclerView // Initialize RecyclerView
        rewardsRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserPoints(currentUser.uid)
            loadRewardsList()
        }

        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
                    // Already in RewardsActivity, do nothing or handle differently if needed
                    true
                }
                R.id.bmiFragment -> {
                    startActivity(Intent(this, BMICalculatorActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserPoints(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userPoints = document.getLong("points") ?: 0
                    binding.currentPointsTextView.text = "Current Points: $userPoints"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RewardsActivity", "Error getting user points: ", exception)
            }
    }

    private fun loadRewardsList() {
        db.collection("rewards")
            .get()
            .addOnSuccessListener { documents ->
                val rewardsList = mutableListOf<Reward>()
                for (document in documents) {
                    val reward = document.toObject(Reward::class.java)
                    rewardsList.add(reward)
                }
                rewardsRecyclerView.adapter = RewardsAdapter(rewardsList, userPoints, ::claimReward)
            }
            .addOnFailureListener { exception ->
                Log.e("RewardsActivity", "Error getting rewards: ", exception)
            }
    }

    private fun claimReward(reward: Reward) {
        val currentUser = auth.currentUser
        if (currentUser != null && userPoints >= reward.requiredPoints) {
            userPoints -= reward.requiredPoints
            db.collection("users").document(currentUser.uid)
                .update("points", userPoints)
                .addOnSuccessListener {
                    binding.currentPointsTextView.text = "Current Points: $userPoints"
                    Toast.makeText(this, "Reward claimed successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("RewardsActivity", "Error updating user points: ", exception)
                    Toast.makeText(this, "Failed to claim reward. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Not enough points to claim this reward.", Toast.LENGTH_SHORT).show()
        }
    }
}
