package com.example.smkituidemoapp

import ClaimedReward
import ClaimedRewardsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                            checkPendingRewards(document.reference)
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
                    reward.status = if (reward.quantity > 0) "claimable" else "On Cooldown"
                    rewardsList.add(reward)
                }
                availableRewardsRecyclerView.adapter = RewardsAdapter(rewardsList, userPoints, ::claimReward)
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

    private fun checkPendingRewards(memberDocRef: DocumentReference) {
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
    }

    private fun claimReward(reward: Reward) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (reward.quantity <= 0) {
                Toast.makeText(this, "This reward is on cooldown and cannot be claimed.", Toast.LENGTH_SHORT).show()
                return
            }

            // Prompt user to enter the quantity they want to claim
            val input = EditText(this)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Claim ${reward.rewardName}")
                .setMessage("Enter the quantity to claim:")
                .setView(input)
                .setPositiveButton("Claim") { _, _ ->
                    val quantityToClaim = input.text.toString().toIntOrNull() ?: 0
                    if (quantityToClaim <= 0 || quantityToClaim > reward.quantity) {
                        Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Proceed to add the reward to pending rewards without deducting points
                    db.collectionGroup("Members")
                        .whereEqualTo("Email", currentUser.email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents != null && !documents.isEmpty) {
                                val document = documents.first()
                                val memberDocRef = document.reference
                                addToPendingRewards(memberDocRef, reward, quantityToClaim)
                            } else {
                                Log.e("RewardsActivity", "No such document to update")
                                Toast.makeText(this, "Failed to add reward to pending rewards. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RewardsActivity", "Error fetching user document: ", exception)
                            Toast.makeText(this, "Failed to add reward to pending rewards. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }

    private fun addToPendingRewards(memberDocRef: DocumentReference, reward: Reward, quantityToClaim: Int) {
        memberDocRef.collection("pending_rewards")
            .get()
            .addOnSuccessListener { documents ->
                val nextIdNumber = documents.size() + 1
                val formattedId = "RWD%03d".format(nextIdNumber) // Format as RWD001, RWD002, etc.

                val totalPointsRequired = reward.requiredPoints * quantityToClaim

                val pendingReward = hashMapOf(
                    "rewardName" to reward.rewardName,
                    "rewardDescription" to reward.rewardDescription,
                    "quantityClaimed" to quantityToClaim,
                    "requiredPoints" to totalPointsRequired,
                    "status" to "pending"
                )

                memberDocRef.collection("pending_rewards")
                    .document(formattedId)
                    .set(pendingReward)
                    .addOnSuccessListener {
                        Log.d("RewardsActivity", "Reward added to pending rewards with ID: $formattedId")

                        Toast.makeText(this, "Reward added to pending rewards.", Toast.LENGTH_SHORT).show()

                        // Update UI to reflect the pending status
                        val currentRewardsAdapter = availableRewardsRecyclerView.adapter as? RewardsAdapter
                        currentRewardsAdapter?.rewardsList?.find { it.rewardName == reward.rewardName }?.status = "pending"
                        currentRewardsAdapter?.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Log.e("RewardsActivity", "Error adding to pending rewards: ", e)
                        Toast.makeText(this, "Failed to add to pending rewards. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RewardsActivity", "Error fetching pending rewards: ", e)
                Toast.makeText(this, "Failed to add to pending rewards. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

}
