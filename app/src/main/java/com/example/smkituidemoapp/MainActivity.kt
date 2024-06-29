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
import com.google.firebase.firestore.FieldValue
import java.util.UUID
import android.app.Activity
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.sency.smkitui.model.SMExercise

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") //formatting dates for Completed Workouts Today Counter

    private var resetTime: Long = 0 //24 hour resetter

    private lateinit var sharedPreferences: SharedPreferences

    private var completedWorkouts = 0

    private val totalWorkouts = 4 // Set the total number of workouts per day

    private lateinit var dailyProgressTextView: TextView

    private lateinit var db: FirebaseFirestore

    private var _binding: MainActivityBinding? = null

    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()  //workout model

    private var smKitUI: SMKitUI? = null //sency motion

    private val tag = this::class.java.simpleName

    private val apiPublicKey = "public_live_BrYk+UxJaahIPdnb" //API key

    private val configurationResult = object : ConfigurationResult { //has to configure first before loading the app
        override fun onFailure() {
            viewModel.setConfigured(false)
            Log.d(tag, "failed to configure")
        }

        override fun onSuccess() {
            viewModel.setConfigured(true)
            Log.d(tag, "succeeded to configure")
        }
    }

    private val exerciseListActivityLauncher = registerForActivityResult(StartActivityForResult()) { result -> //workout launcher
        if (result.resultCode == Activity.RESULT_OK) {
            val workoutId = result.data?.getStringExtra("workoutId")
            startWorkout(workoutId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dailyProgressTextView = findViewById(R.id.dailyProgressMonitorTextView)

        db = FirebaseFirestore.getInstance() //database initialization

        sharedPreferences = getSharedPreferences("workout_tracker", Context.MODE_PRIVATE)

        resetTime = sharedPreferences.getLong("resetTime", 0)

        if (LocalDate.now().format(formatter) !=
            LocalDate.ofEpochDay(resetTime / 86400000).format(formatter)
        ) {
            sharedPreferences.edit().putBoolean("updatedToday", false).apply()
            resetTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            sharedPreferences.edit().putLong("resetTime", resetTime).apply()
            resetWorkoutCounter()
        }

        dailyProgressTextView = findViewById(R.id.dailyProgressMonitorTextView)

        // Load workout data from Firestore
        loadUserData()
        requestPermissions()
        setClickListeners()

        val bottomNavigationView = binding.bottomNavigation

        bottomNavigationView.setOnItemSelectedListener { item -> //navbar
            when (item.itemId) {
                R.id.homeFragment -> {
                    // ...
                    true
                }
                R.id.profileFragment -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setClickListeners() {
        binding.upperBodyButton.setOnClickListener { //workout categories
            navigateToExerciseList("upperBody", "Upper Body Workout", viewModel.upperBodyWorkout())
        }
        binding.coreButton.setOnClickListener {
            navigateToExerciseList("core", "Core Workout", viewModel.coreWorkout())
        }
        binding.legsButton.setOnClickListener {
            navigateToExerciseList("legs", "Leg Workout", viewModel.legsWorkout())
        }
        binding.cardioButton.setOnClickListener {
            navigateToExerciseList("cardio", "Cardio Workout", viewModel.cardioWorkout())
        }
    }

    private fun navigateToExerciseList(workoutId: String, workoutName: String, exercises: List<SMExercise>) {
        val exerciseNames = exercises.map { it.prettyName }
        val intent = Intent(this, ExerciseListActivity::class.java).apply {
            putStringArrayListExtra("exercises", ArrayList(exerciseNames))
            putExtra("workoutId", workoutId)
            putExtra("workoutName", workoutName)
        }
        exerciseListActivityLauncher.launch(intent)
    }

    private fun startWorkout(workoutId: String?) { //maps the workouts
        smKitUI?.let {
            val workout = when (workoutId) {
                "upperBody" -> SMWorkout(
                    id = "upperBody",
                    name = "Upper Body Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.upperBodyWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                "core" -> SMWorkout(
                    id = "core",
                    name = "Core Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.coreWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                "legs" -> SMWorkout(
                    id = "legs",
                    name = "Leg Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.legsWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                "cardio" -> SMWorkout(
                    id = "cardio",
                    name = "Cardio Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.cardioWorkout(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                else -> null
            }
            workout?.let { it1 ->
                it.startWorkout(it1, this)
            }
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
        smKitUI = SMKitUI.Configuration(baseContext)
            .setUIKey(apiPublicKey)
            .configure(configurationResult)
    }

    override fun didExitWorkout(summary: WorkoutSummaryData) {
        Log.d(tag, "didExitWorkout: $summary")
        Toast.makeText(baseContext, "Workout Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun exerciseDidFinish(data: ExerciseData) {
        Log.d(tag, "exerciseDidFinish: $data")
    }

    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun workoutDidFinish(summary: WorkoutSummaryData) { //handles events upon finishing the workout
        Log.d(tag, "workoutDidFinish: $summary")

        val exercises = summary.exercises
        val totalExercises = exercises.size
        var totalScore = 0f

        for (exercise in exercises) {
            val exerciseScore = exercise.totalScore
            Log.d(tag, "Exercise ${exercise.prettyName}: Score = $exerciseScore")
            totalScore += exerciseScore
        }

        val averageScore = if (totalExercises > 0) totalScore / totalExercises else 0f
        val points = calculatePoints(averageScore)

        Log.d(tag, "Average Score: $averageScore, Points: $points")

        updatePointsInFirestore(points)

        // Increment the completed workouts counter
        completedWorkouts++
        sharedPreferences.edit().putInt("completedWorkouts", completedWorkouts).apply()
        updateDailyProgressText()

        // Update the workout counter in Firestore
        updateWorkoutCounterInFirestore(completedWorkouts)
        Toast.makeText(baseContext, "Points Collected: $points!", Toast.LENGTH_SHORT).show()
    }

    private fun updateWorkoutCounterInFirestore(completedWorkouts: Int) { //update daily workout counter
        val userId = getUserId()
        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("completedWorkoutsToday", completedWorkouts)
                .addOnSuccessListener {
                    Log.d(tag, "Completed workouts updated in Firestore: $completedWorkouts")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error updating completed workouts in Firestore", e)
                }
        }
    }

    private fun resetWorkoutCounter() { //resets counter
        completedWorkouts = 0
        sharedPreferences.edit().putInt("completedWorkouts", 0).apply()
        updateDailyProgressText()
        // Update Firestore
        updateWorkoutCounterInFirestore(0)
    }

    private fun updateDailyProgressText() {
        dailyProgressTextView.text = "Completed Workouts Today: $completedWorkouts/$totalWorkouts"
    }

    private fun updatePointsInFirestore(points: Int) { //updates points in database upon finishing the workout
        val userId = getUserId()
        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("points", FieldValue.increment(points.toDouble()))
                .addOnSuccessListener {
                    Log.d(tag, "Points updated in Firestore: $points")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error updating points in Firestore", e)
                }
        }
    }

    private fun calculatePoints(averageScore: Float): Int {
        return when {
            averageScore >= 90 -> 50 //possible points depending on performance
            averageScore >= 80 -> 40
            averageScore >= 70 -> 30
            averageScore >= 60 -> 20
            else -> 10
        }
    }

    private fun loadUserData() { //loads user data from database
        val userId = getUserId()
        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        completedWorkouts = document.getLong("completedWorkoutsToday")?.toInt() ?: 0
                        sharedPreferences.edit().putInt("completedWorkouts", completedWorkouts).apply()
                        updateDailyProgressText()
                    } else {
                        Log.d(tag, "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error fetching document", e)
                }
        }
    }

    private fun getUserId(): String? {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            userId = sharedPreferences.getString("userId", null)

            if (userId == null) {
                userId = UUID.randomUUID().toString()
                sharedPreferences.edit().putString("userId", userId).apply()
            }
        }
        return userId
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
