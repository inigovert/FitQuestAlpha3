package com.example.smkituidemoapp

import ClaimedReward
import ClaimedRewardsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smkituidemoapp.databinding.ActivityRewardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var availableRewardsRecyclerView: RecyclerView
    private lateinit var claimedRewardsRecyclerView: RecyclerView

    private var userPoints: Long = 0
    private var gymId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        availableRewardsRecyclerView = binding.availableRewardsRecyclerView
        claimedRewardsRecyclerView = binding.claimedRewardsRecyclerView

        availableRewardsRecyclerView.layoutManager = LinearLayoutManager(this)
        claimedRewardsRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserGymAndPoints(currentUser.email)
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
                R.id.rewardsFragment -> true
                R.id.bmiFragment -> {
                    startActivity(Intent(this, BMICalculatorActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserGymAndPoints(email: String?) {
        if (email != null) {
            db.collectionGroup("Members")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.first()
                        userPoints = document.getLong("Points") ?: 0
                        binding.currentPointsTextView.text = "Current Points: $userPoints"

                        gymId = document.reference.parent.parent?.id
                        if (gymId != null) {
                            loadRewardsList(gymId!!)
                            loadClaimedRewards(document.reference)
                        } else {
                            Log.e("RewardsActivity", "Gym ID is null")
                        }
                    } else {
                        Log.d("RewardsActivity", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("RewardsActivity", "Error fetching document", e)
                }
        }
    }

    private fun loadRewardsList(gymId: String) {
        db.collection("Gym").document(gymId).collection("Rewards")
            .get()
            .addOnSuccessListener { documents ->
                val rewardsList = mutableListOf<Reward>()
                for (document in documents) {
                    val reward = document.toObject(Reward::class.java)
                    reward.status = "claimable"
                    rewardsList.add(reward)
                }
                availableRewardsRecyclerView.adapter = RewardsAdapter(rewardsList, userPoints, ::claimReward)
                // Ensure checkPendingRewards is called after the adapter is set
                checkPendingRewards()
            }
            .addOnFailureListener { exception ->
                Log.e("RewardsActivity", "Error getting rewards: ", exception)
            }
    }

    private fun loadClaimedRewards(memberDocRef: DocumentReference) {
        memberDocRef.collection("claimed_rewards")
            .get()
            .addOnSuccessListener { documents ->
                val claimedRewardsList = mutableListOf<ClaimedReward>()
                for (document in documents) {
                    val claimedReward = ClaimedReward(
                        rewardName = document.getString("rewardName") ?: "Unknown",
                        rewardDescription = document.getString("rewardDescription") ?: "No Description",
                        dateClaimed = document.id // Document ID used as date
                    )
                    claimedRewardsList.add(claimedReward)
                }
                claimedRewardsRecyclerView.adapter = ClaimedRewardsAdapter(claimedRewardsList)
            }
            .addOnFailureListener { e ->
                Log.e("RewardsActivity", "Error fetching claimed rewards", e)
            }
    }

    private fun checkPendingRewards() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collectionGroup("Members")
                .whereEqualTo("Email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.first()
                        val memberDocRef = document.reference

                        memberDocRef.collection("pending_rewards")
                            .get()
                            .addOnSuccessListener { pendingRewards ->
                                val currentRewardsAdapter = availableRewardsRecyclerView.adapter as? RewardsAdapter
                                currentRewardsAdapter?.let { adapter ->
                                    for (pendingReward in pendingRewards) {
                                        val rewardName = pendingReward.id
                                        adapter.rewardsList.find { it.rewardName == rewardName }?.status = "pending"
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("RewardsActivity", "Error getting pending rewards", e)
                            }
                    } else {
                        Log.e("RewardsActivity", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("RewardsActivity", "Error fetching user document", e)
                }
        }
    }

    private fun claimReward(reward: Reward) {
        val currentUser = auth.currentUser
        if (currentUser != null && userPoints >= reward.requiredPoints) {
            db.collectionGroup("Members")
                .whereEqualTo("Email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.first()
                        val memberDocRef = document.reference
                        checkPendingRewardsAndAdd(memberDocRef, reward)
                    } else {
                        Log.e("RewardsActivity", "No such document to update")
                        Toast.makeText(this, "Failed to claim reward. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("RewardsActivity", "Error fetching user document: ", exception)
                    Toast.makeText(this, "Failed to claim reward. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Not enough points to claim this reward.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPendingRewardsAndAdd(memberDocRef: DocumentReference, reward: Reward) {
        memberDocRef.collection("pending_rewards").document(reward.rewardName)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Toast.makeText(this, "Reward is already pending.", Toast.LENGTH_SHORT).show()
                } else {
                    addToPendingRewards(memberDocRef, reward)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RewardsActivity", "Error checking pending rewards", e)
            }
    }

    private fun addToPendingRewards(memberDocRef: DocumentReference, reward: Reward) {
        val pendingReward = hashMapOf(
            "rewardName" to reward.rewardName,
            "rewardDescription" to reward.rewardDescription,
            "requiredPoints" to reward.requiredPoints,
            "status" to "pending"
        )

        memberDocRef.collection("pending_rewards")
            .document(reward.rewardName)
            .set(pendingReward)
            .addOnSuccessListener {
                Log.d("RewardsActivity", "Reward added to pending rewards")
                Toast.makeText(this, "Reward added to pending rewards.", Toast.LENGTH_SHORT).show()
                // Update the status of the reward in the rewards list
                val currentRewardsAdapter = availableRewardsRecyclerView.adapter as? RewardsAdapter
                currentRewardsAdapter?.let { adapter ->
                    adapter.rewardsList.find { it.rewardName == reward.rewardName }?.status = "pending"
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RewardsActivity", "Error adding to pending rewards", e)
            }
    }
}
