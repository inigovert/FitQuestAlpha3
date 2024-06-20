package com.example.smkituidemoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smkituidemoapp.databinding.MainActivityBinding
import com.example.smkituidemoapp.viewModels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sency.smbase.core.listener.ConfigurationResult
import com.sency.smkitui.BuildConfig
import com.sency.smkitui.SMKitUI
import com.sency.smkitui.listener.SMKitUIWorkoutListener
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.WorkoutSummaryData
import com.sency.smkitui.model.SMExercise
import com.google.firebase.firestore.SetOptions
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    private val REQUEST_CODE_CHOOSE_WORKOUT = 101

    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var smKitUI: SMKitUI? = null

    private val tag = this::class.java.simpleName

    private val apiPublicKey = when (!BuildConfig.DEBUG) {
        true -> "public_live_BrYk+UxJaahIPdnb"
        else -> "public_live_#gdz3t)mW#$39Crs"
    }

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()

    private val configurationResult = object : ConfigurationResult {
        override fun onFailure() {
            viewModel.setConfigured(false)
            Log.d("Activity", "failed to configure")
        }

        override fun onSuccess() {
            viewModel.setConfigured(true)
            Log.d("Activity", "succeeded to configure")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions()
        observeConfiguration()
        setClickListeners()
        fetchAndDisplayUserPoints()
    }

    private fun fetchAndDisplayUserPoints() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val points = document.getDouble("points") ?: 0.0
                        binding.pointsTextView.text = "Points: $points"
                    } else {
                        Log.d(tag, "No user document found. Creating one with initial points.")
                        createUserDocumentInFirestore(currentUser.uid, 0.0)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error getting user data: ", exception)
                    showToast("Error fetching user points")
                }
        } else {
            binding.pointsTextView.text = "Points: 0"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startWorkoutForExercise(exercise: SMExercise) {
        if (smKitUI == null) {
            showToast("Please configure first")
            return
        }

        val workout = SMWorkout(
            id = exercise.name.lowercase().replace(" ", "_") + "_workout",
            name = "${exercise.name} Workout",
            workoutIntro = Uri.EMPTY,
            soundtrack = Uri.EMPTY,
            exercises = listOf(exercise),
            workoutClosure = Uri.EMPTY
        )
        smKitUI?.startWorkout(workout, this)
    }

    private fun setClickListeners() {

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.bmiCalculatorButton.setOnClickListener {
            startActivity(Intent(this, BMICalculatorActivity::class.java))
        }
        binding.chooseWorkoutButton.setOnClickListener {
            val intent = Intent(this, ChooseWorkoutActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_WORKOUT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CHOOSE_WORKOUT && resultCode == RESULT_OK) {
            val selectedExerciseIndex = data?.getIntExtra("selectedExerciseIndex", -1) ?: -1

            if (selectedExerciseIndex >= 0) {
                val selectedExercise = viewModel.exercises()[selectedExerciseIndex]
                smKitUI?.startWorkout(SMWorkout(
                    id = selectedExercise.name.lowercase().replace(" ", "_") + "_workout",
                    name = "${selectedExercise.name} Workout",
                    workoutIntro = Uri.EMPTY,
                    soundtrack = Uri.EMPTY,
                    exercises = listOf(selectedExercise), // Use the selected exercise
                    workoutClosure = Uri.EMPTY
                ), this)
            }
        }
    }


    private fun observeConfiguration() {
        viewModel.configured.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.INVISIBLE
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
        binding.progressBar.visibility = View.VISIBLE
        smKitUI = SMKitUI.Configuration(baseContext)
            .setUIKey(apiPublicKey)
            .configure(configurationResult)
    }

    override fun didExitWorkout(summary: WorkoutSummaryData) {
        Log.d(tag, "didExitWorkout: $summary")
    }

    override fun exerciseDidFinish(data: ExerciseData) {
        Log.d(tag, "exerciseDidFinish: $data")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val selectedExercise = viewModel.exercises().find { it.name == data.name } // Find exercise using data.name

            if (selectedExercise != null) {
                val pointsToAward = if (selectedExercise.repBased) {
                    calculateRepBasedPoints(data.totalScore.toDouble(), selectedExercise.name) // Calculate rep-based points
                } else {
                    calculateDurationBasedPoints(data.totalTime, selectedExercise.name) // Calculate duration-based points
                }
                processScoreForRewards(userId, pointsToAward) // Award calculated points
                Toast.makeText(this, "You earned ${pointsToAward.toInt()} points!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(tag, "Exercise not found in the list, no points awarded.")
            }
        }
    }


    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    override fun workoutDidFinish(summary: WorkoutSummaryData) {
        Log.d(tag, "workoutDidFinish: $summary")
       // val userId = FirebaseAuth.getInstance().currentUser?.uid
       // if (userId != null) {
            // Pass the score as a Double (automatic conversion)
            //processScoreForRewards(userId, summary.score.toDouble())
        //}
    }


    private fun processScoreForRewards(userId: String, exerciseScore: Double) {
        Log.d(tag, "Processing score for user: $userId")
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains("points")) {
                    val currentPoints = document.getDouble("points") ?: 0.0
                    val newPoints = currentPoints + exerciseScore // Directly add the exerciseScore
                    updatePointsInDatabase(userId, newPoints)
                } else {
                    createUserDocumentInFirestore(userId, exerciseScore) // Pass exerciseScore directly
                }
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error getting user document", exception)
                showToast("Error updating points")
            }
    }
    private fun calculateRepBasedPoints(totalScore: Double, exerciseName: String): Double {
        // Example calculation - adjust the ranges and points as needed
        val estimatedReps = when {
            totalScore >= 90 -> (15..20).random().toDouble() // Randomly choose between 15 and 20 reps
            totalScore >= 70 -> (10..14).random().toDouble()
            totalScore >= 50 -> (5..9).random().toDouble()
            else -> 0.0 // No points for very low scores
        }
        return estimatedReps * pointsPerRep(exerciseName)
    }

    private fun pointsPerRep(exerciseName: String): Double {
        // Logic to determine points per rep based on the exercise
        return when (exerciseName) {
            "Squats" -> 1.0
            "High Knees" -> 2.0
            // ... add more exercises as needed
            else -> 0.0 // Default for unknown exercises
        }
    }

    private fun calculateDurationBasedPoints(durationSeconds: Double, exerciseName: String): Double {
        // Example calculation - adjust points per second and thresholds
        val pointsPerSecond = when (exerciseName) {
            "Plank" -> 0.5
            else -> 0.0 // Default for unknown exercises
        }
        return durationSeconds * pointsPerSecond
    }

    private fun updatePointsInDatabase(userId: String, newPoints: Double) {
        val userRef = db.collection("users").document(userId)
        userRef.set(hashMapOf("points" to newPoints), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("MainActivity", "Points updated successfully")
                fetchAndDisplayUserPoints()  // Refresh UI
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error updating points", e)
                showToast("Error updating points")
            }
    }

    private fun createUserDocumentInFirestore(userId: String, initialPoints: Double) {
        val userRef = db.collection("users").document(userId)
        val userData = hashMapOf(
            "userId" to userId,
            "points" to initialPoints
        )
        userRef.set(userData)
            .addOnSuccessListener { Log.d(tag, "User document created with ID: $userId") }
            .addOnFailureListener { e -> Log.e(tag, "Error creating user document", e) }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in PERMISSIONS_REQUIRED && !it.value) permissionGranted = false
        }
        if (permissionGranted && permissions.isNotEmpty()) {
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

