package com.example.gymmembership

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.InitialLoginActivity
import com.example.smkituidemoapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.smkituidemoapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val currentUser = auth.currentUser
        val gymId = intent.getStringExtra("GYM_ID")

        if (currentUser != null && gymId != null) {
            val userRef = db.collection("Gym").document(gymId).collection("Members").document(currentUser.email!!)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.firstNameTextView.text = "First Name: ${document.getString("firstName") ?: "N/A"}"
                        binding.lastNameTextView.text = "Last Name: ${document.getString("lastName") ?: "N/A"}"
                        binding.emailTextView.text = "Email: ${document.getString("email") ?: "N/A"}"
                        binding.pointsTextView.text = "Points: ${document.getLong("points") ?: 0}"

                        fetchWorkoutHistory(currentUser.email!!, gymId)
                    } else {
                        Log.e("ProfileActivity", "Document does not exist or is null")
                        binding.firstNameTextView.text = "First Name: N/A"
                        binding.lastNameTextView.text = "Last Name: N/A"
                        binding.emailTextView.text = "Email: N/A"
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

    private fun fetchWorkoutHistory(email: String, gymId: String) {
        db.collection("Gym")
            .document(gymId)
            .collection("Members")
            .document(email)
            .collection("WorkoutHistory")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("ProfileActivity", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Error getting workout history: ", e)
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}
