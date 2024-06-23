package com.example.smkituidemoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smkituidemoapp.databinding.MainActivityBinding
import com.example.smkituidemoapp.viewModels.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sency.smbase.core.listener.ConfigurationResult
import com.sency.smkitui.SMKitUI
import com.sency.smkitui.listener.SMKitUIWorkoutListener
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.WorkoutSummaryData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import java.time.ZoneOffset
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue



class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    //VARIABLES
    //private val WORKOUT = 101

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Track the number of workouts completed today
//    private var completedWorkoutsToday = 0

    // Time when the tracker resets
    private var resetTime: Long = 0

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var db: FirebaseFirestore

    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var smKitUI: SMKitUI? = null

    private val tag = this::class.java.simpleName

    private val apiPublicKey = "public_live_BrYk+UxJaahIPdnb"

    private val configurationResult = object : ConfigurationResult {
        override fun onFailure() {
            viewModel.setConfigured(false)
            Log.d(tag, "failed to configure")
        }

        override fun onSuccess() {
            viewModel.setConfigured(true)
            Log.d(tag, "succeeded to configure")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    //ONCREATES
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences("workout_tracker", Context.MODE_PRIVATE)

        resetTime = sharedPreferences.getLong("resetTime", 0)

        // If it's a new day, reset the count
        if (LocalDate.now().format(formatter) !=
            LocalDate.ofEpochDay(resetTime / 86400000).format(formatter)
        ) { // 86400000 milliseconds in a day
            resetTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() // Update reset time
            sharedPreferences.edit().putLong("resetTime", resetTime).apply()
        }

        updateDailyTrackerUI()
        requestPermissions()
        setClickListeners()
        resetWorkoutCounterInFirestore()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    //CLICKLISTENERS | BUTTON MAPPINGS
    private fun setClickListeners() {
        binding.startAssessment.setOnClickListener {
            smKitUI?.startAssessment(this)
        }

        binding.upperBodyButton.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "upperBody",
                    name = "Upper Body Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.upperBodyWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                updateDailyTrackerUI()
                it.startWorkout(smWorkout, this)
            }
        }
        binding.coreButton.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "core",
                    name = "Core Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.coreWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                updateDailyTrackerUI()
                it.startWorkout(smWorkout, this)
            }
        }
        binding.legsButton.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "legs",
                    name = "Leg Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.legsWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                updateDailyTrackerUI()
                it.startWorkout(smWorkout, this)
            }
        }
        binding.cardioButton.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "cardio",
                    name = "Cardio Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.cardioWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                updateDailyTrackerUI()
                it.startWorkout(smWorkout, this)
            }
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bmiCalculatorButton.setOnClickListener {
            startActivity(Intent(this, BMICalculatorActivity::class.java))
        }
        binding.rewardsButton.setOnClickListener{
            startActivity(Intent(this, RewardsActivity::class.java))
        }
    }
    private fun requestPermissions() {
        if (!hasPermissions(baseContext)) {
            launcher.launch(PERMISSIONS_REQUIRED)
        } else {
            configureKit()
        }
    }

    private fun configureKit() {
        //binding.progressBar.visibility = View.VISIBLE
        smKitUI = SMKitUI.Configuration(baseContext)
            .setUIKey(apiPublicKey)
            .configure(configurationResult)
    }

    override fun didExitWorkout(summary: WorkoutSummaryData) {
        Log.d(tag, "didExitWorkout: $summary")
    }

    override fun exerciseDidFinish(data: ExerciseData) {
        Log.d(tag, "exerciseDidFinish: $data")
    }

    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    override fun workoutDidFinish(summary: WorkoutSummaryData) {
        Log.d(tag, "workoutDidFinish: $summary")

        val exercises = summary.exercises
        val totalExercises = exercises.size
        var totalScore = 0f

        // Calculate and log individual exercise scores
        for (exercise in exercises) {
            val exerciseScore = exercise.totalScore
            Log.d(tag, "Exercise ${exercise.prettyName}: Score = $exerciseScore")
            totalScore += exerciseScore
        }

        val averageScore = if (totalExercises > 0) totalScore / totalExercises else 0f
        val points = calculatePoints(averageScore)

        Log.d(tag, "Average Score: $averageScore, Points: $points")

        // Update points in Firestore
        updatePointsInFirestore(points)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDailyTrackerUI() {
        val today = LocalDate.now().format(formatter)
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID

        if (userId != null) {
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // User document exists
                    if (document.contains("workoutsCompleted")) {
                        // Field exists, increment
                        userRef.update("workoutsCompleted", FieldValue.increment(1))
                            .addOnSuccessListener {
                                // Retrieve updated value and display
                                fetchAndDisplayCompletedWorkouts(userId, today)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(tag, "Error incrementing workoutsCompleted", exception)
                                showToast("Error updating workout count")
                            }
                    } else {
                        // Field doesn't exist, create it with initial value 1
                        userRef.update("workoutsCompleted", 1)
                            .addOnSuccessListener {
                                // Retrieve updated value and display
                                fetchAndDisplayCompletedWorkouts(userId, today)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(tag, "Error creating workoutsCompleted field", exception)
                                showToast("Error updating workout count")
                            }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e(tag, "Error getting user document", exception)
            }
        }
    }

    private fun updatePointsInFirestore(points: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("points", FieldValue.increment(points.toDouble()))
                .addOnSuccessListener {
                    Log.d(tag, "Points updated in Firestore: $points")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error updating points in Firestore", e)
                    // Consider showing an error message to the user
                }
        }
    }
    private fun calculatePoints(averageScore: Float): Int {
        return when {
            averageScore >= 90 -> 50
            averageScore >= 80 -> 40
            averageScore >= 70 -> 30
            averageScore >= 60 -> 20
            else -> 10
        }
    }
    private fun resetWorkoutCounterInFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID

        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("workoutsCompleted", 0)
                .addOnSuccessListener {
                    Log.d(tag, "Workout counter reset in Firestore for user: $userId")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error resetting workout counter", e)
                    // Handle the error appropriately
                }
        }
    }

    // Helper function to fetch and display the completed workouts
    private fun fetchAndDisplayCompletedWorkouts(userId: String, today: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val completedWorkoutsToday = document.getLong("workoutsCompleted") ?: 0
                    val totalWorkoutCount = 4
                    binding.dailyProgressMonitorTextView.text = "Completed Workouts Today: $completedWorkoutsToday/$totalWorkoutCount"
                } else {
                    Log.d(tag, "Error fetching completed workouts")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error getting user data: ", exception)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all {
                it.key in PERMISSIONS_REQUIRED && it.value
            }
            if (permissionGranted) {
                configureKit()
            } else {
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    companion object {
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
