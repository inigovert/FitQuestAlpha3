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
import java.util.UUID
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.sency.smkitui.model.SMExercise

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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

    private val exerciseListActivityLauncher = registerForActivityResult(StartActivityForResult()) { result ->
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

        db = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences("workout_tracker", Context.MODE_PRIVATE)

        resetTime = sharedPreferences.getLong("resetTime", 0)

        if (LocalDate.now().format(formatter) !=
            LocalDate.ofEpochDay(resetTime / 86400000).format(formatter)
        ) {
            sharedPreferences.edit().putBoolean("updatedToday", false).apply()
            resetTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            sharedPreferences.edit().putLong("resetTime", resetTime).apply()
        }

        updateDailyTrackerUI()
        requestPermissions()
        setClickListeners()
        resetWorkoutCounterInFirestore()
    }

    private fun setClickListeners() {
        binding.upperBodyButton.setOnClickListener {
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

    private fun startWorkout(workoutId: String?) {
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
    }

    override fun exerciseDidFinish(data: ExerciseData) {
        Log.d(tag, "exerciseDidFinish: $data")
    }

    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun workoutDidFinish(summary: WorkoutSummaryData) {
        Log.d(tag, "workoutDidFinish: $summary")

        if (!sharedPreferences.getBoolean("updatedToday", false)) {
            incrementWorkoutCounter()
            sharedPreferences.edit().putBoolean("updatedToday", true).apply()
        }

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
    }

    fun onFinishSession(summary: WorkoutSummaryData) {
        Log.d(tag, "onFinishExercise $summary")
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDailyTrackerUI() {
        val today = LocalDate.now().format(formatter)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            initializeWorkoutCounter(userId)
            fetchAndDisplayCompletedWorkouts(userId, today)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun incrementWorkoutCounter() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userRef = db.collection("users").document(userId)

            Log.d(tag, "Increment Workout Counter: Checking if updatedToday is false.")

            if (!sharedPreferences.getBoolean("updatedToday", false)) {
                userRef.update("workoutsCompleted", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Log.d(tag, "Incremented workoutsCompleted for user: $userId")
                        fetchAndDisplayCompletedWorkouts(userId, LocalDate.now().format(formatter))
                        sharedPreferences.edit().putBoolean("updatedToday", true).apply()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(tag, "Error incrementing workoutsCompleted", exception)
                        showToast("Error updating workout count")
                    }
            } else {
                Log.d(tag, "Workout counter not incremented because updatedToday is true.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeWorkoutCounter(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists() || !document.contains("workoutsCompleted")) {
                userRef.update("workoutsCompleted", 0)
                    .addOnSuccessListener {
                        Log.d(tag, "Initialized workoutsCompleted to 0 for user: $userId")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(tag, "Error initializing workoutsCompleted field", exception)
                        showToast("Error initializing workout count")
                    }
            }
        }.addOnFailureListener { exception ->
            Log.e(tag, "Error getting user document", exception)
        }
    }

    private fun updatePointsInFirestore(points: Int) {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        if(userId == null) {
            userId = sharedPreferences.getString("userId", null)

            if(userId == null) {
                userId = UUID.randomUUID().toString()
                sharedPreferences.edit().putString("userId", userId).apply()
            }
        }
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
            averageScore >= 90 -> 50
            averageScore >= 80 -> 40
            averageScore >= 70 -> 30
            averageScore >= 60 -> 20
            else -> 10
        }
    }

    private fun resetWorkoutCounterInFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("workoutsCompleted", 0)
                .addOnSuccessListener {
                    Log.d(tag, "Workout counter reset in Firestore for user: $userId")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error resetting workout counter", e)
                }
        }
    }

    private fun fetchAndDisplayCompletedWorkouts(userId: String, today: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val completedWorkoutsToday = document.getLong("workoutsCompleted") ?: 0
                    val totalWorkoutCount = 4
                    // binding.dailyProgressMonitorTextView.text = "Completed Workouts Today: $completedWorkoutsToday/$totalWorkoutCount"
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
