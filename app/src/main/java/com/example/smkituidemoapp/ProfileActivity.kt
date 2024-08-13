package com.example.smkituidemoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private val datesWithLogs = mutableSetOf<LocalDate>() // Track dates with workout logs

    @RequiresApi(Build.VERSION_CODES.O)
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
                R.id.profileFragment -> true
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
            fetchWorkoutLogs(currentUser.uid)
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
            val selectedDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", dayOfMonth)}"
            currentUser?.uid?.let {
                fetchWorkoutLogDetails(it, selectedDate)
            } ?: Toast.makeText(this, "Please log in to view workout logs", Toast.LENGTH_SHORT).show()
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

    private fun fetchGymIdAndMemberId(callback: (gymId: String?, memberId: String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                db.collectionGroup("Members")
                    .whereEqualTo("Email", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.first()
                            val gymId = document.reference.parent.parent?.id
                            val memberId = document.id
                            callback(gymId, memberId)
                        } else {
                            callback(null, null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ProfileActivity", "Failed to retrieve gym and member IDs: ", exception)
                        callback(null, null)
                    }
            } else {
                callback(null, null)
            }
        } else {
            callback(null, null)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWorkoutLogs(memberId: String) {
        fetchGymIdAndMemberId { gymId, _ ->
            if (gymId != null) {
                db.collection("Gym")
                    .document(gymId)
                    .collection("Members")
                    .document(memberId)
                    .collection("workout_logs")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                val timestamp = document.getTimestamp("date")
                                if (timestamp != null) {
                                    val localDate = timestamp.toDate().toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    highlightCalendarDate(localDate.toString())
                                }
                            }
                        } else {
                            Log.d("ProfileActivity", "No workout logs found")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("ProfileActivity", "Failed to retrieve workout logs: ", exception)
                    }
            } else {
                Toast.makeText(this, "Gym ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun highlightCalendarDate(date: String) {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        datesWithLogs.add(localDate)

        // Highlighting logic: Use your preferred method to highlight dates on the calendar.
        // Since Android's default CalendarView doesn't support direct highlighting, you'll
        // have to implement custom logic, potentially with a library or custom view overlay.

        // Example (toast-based notification):
        Toast.makeText(this, "Workout log exists on $localDate", Toast.LENGTH_SHORT).show()

        // Log for verification
        Log.d("ProfileActivity", "Highlighting date: $localDate")
    }

    private fun fetchWorkoutLogDetails(memberId: String, selectedDate: String) {
        val gymId = "GYM001" // Replace with actual method to fetch gymId

        db.collection("Gym")
            .document(gymId)
            .collection("Members")
            .document(memberId)
            .collection("workout_logs")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val logs = StringBuilder()
                    for (document in documents) {

                        val pointsEarned = document.getLong("pointsEarned") ?: 0
                        logs.append("Points: $pointsEarned\n\n")
                    }
                    showWorkoutLogsPopup(selectedDate, logs.toString())
                } else {
                    Toast.makeText(this, "No workouts found for $selectedDate", Toast.LENGTH_SHORT).show()
                    Log.d("ProfileActivity", "No workouts found for $selectedDate")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileActivity", "Failed to retrieve workout logs: ", exception)
            }
    }

    private fun showWorkoutLogsPopup(date: String, logs: String) {
        // Use AlertDialog or any other popup to display the workout logs
        val dialog = AlertDialog.Builder(this)
            .setTitle("Workout Logs for $date")
            .setMessage(logs)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }
}
