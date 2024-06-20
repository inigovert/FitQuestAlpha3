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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sency.smbase.core.listener.ConfigurationResult
import com.sency.smkitui.BuildConfig
import com.sency.smkitui.SMKitUI
import com.sency.smkitui.listener.SMKitUIWorkoutListener
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.WorkoutSummaryData
import com.sency.smkitui.model.SMExercise
import com.example.smkituidemoapp.databinding.ActivityChooseWorkoutBinding

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    private val REQUEST_CODE_CHOOSE_WORKOUT = 101

    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!

    private val viewModel : MainViewModel by viewModels()

    private var smKitUI: SMKitUI? = null

    private val tag = this::class.java.simpleName

    private val apiPublicKey = when (!BuildConfig.DEBUG) {
        true -> "public_live_BrYk+UxJaahIPdnb"
        else -> "public_live_#gdz3t)mW#\$39Crs"
    }

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
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val points = document.getDouble("points") ?: 0.0
                        binding.pointsTextView.text = "Points: $points"
                    } else {
                        Log.d(tag, "No user document found. Creating one with initial points.")
                        createUserDocumentInFirestore(currentUser.uid, 0.0) // Create with 0 points
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error getting user data: ", exception)
                    // Handle the error (e.g., show a toast or log message)
                }
        } else {
            binding.pointsTextView.text = "Points: 0"
            // You might want to handle the case where the user is not logged in here.
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    // Function to update points in Firestore
//    private fun updatePointsInDatabase(userId: String, newPoints: Long) {
//        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
//        userRef.update("points", newPoints) // Update points in Firestore
//            .addOnSuccessListener { Log.d("MainActivity", "Points updated successfully") }
//            .addOnFailureListener { e -> Log.e("MainActivity", "Error updating points", e) }
//    }

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
        binding.startAssessment.setOnClickListener {
            smKitUI?.startAssessment(this)
        }
//        binding.startCustomWorkout.setOnClickListener { former custom workout button
//            smKitUI?.let {
//                val smWorkout = SMWorkout(
//                    id = "",
//                    name = "TEST",
//                    workoutIntro = Uri.EMPTY ,
//                    soundtrack = Uri.EMPTY ,
//                    exercises = viewModel.exercises(),
//                    workoutClosure = Uri.EMPTY
//                )
//                it.startWorkout(smWorkout, this)
//            }
//        }
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

//        binding.squatButton.setOnClickListener { former squat button
//            startWorkoutForExercise(viewModel.exercises()[1]) // Squat is at index 1
//        }

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
            if (selectedExerciseIndex != -1) {
                startWorkoutForExercise(viewModel.exercises()[selectedExerciseIndex])
            }
        }
    }

    private fun observeConfiguration() {
        viewModel.configured.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.INVISIBLE
                binding.startAssessment.visibility = View.VISIBLE
                //binding.startCustomWorkout.visibility = View.VISIBLE
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
        if (viewModel.awardPointsPerExercise) { // Check if awarding points per exercise is enabled
            val userId = FirebaseAuth.getInstance().currentUser?.uid // Check for logged-in user
            if (userId != null) {
                processScoreForRewards(userId, data.totalScore) // Pass the exercise score
            }
        }
    }

    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    override fun workoutDidFinish(summary: WorkoutSummaryData) {
        Log.d(tag, "workoutDidFinish: $summary")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

//        if (userId != null) {
//            processScoreForRewards(userId, summary.score)
//        }

    }
    private fun processScoreForRewards(userId: String, exerciseScore: Float) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Document exists, update points
                    val currentPoints = document.getLong("points") ?: 0
                    val pointsToAward = calculateRewardPoints(exerciseScore)
                    val newPoints = (currentPoints + pointsToAward).toDouble()
                    updatePointsInDatabase(userId, newPoints)
                } else {
                    // Document doesn't exist, create it with initial points
                    createUserDocumentInFirestore(userId, (calculateRewardPoints(exerciseScore).toDouble()))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error getting user document", exception)
                // Handle the error appropriately (e.g., show a message to the user)
                showToast("Error updating points")
            }
        // Update the user's points in Firestore
        fetchAndDisplayUserPoints()
    }
    private fun calculateRewardPoints(exerciseScore: Float): Long {
        // Example calculation - adjust as needed
        return when {
            exerciseScore >= 90 -> 15 // Award more points for higher scores
            exerciseScore >= 70 -> 10
            else -> 5
        }.toLong()  // Ensure the result is a Long
    }
    private fun createUserDocumentInFirestore(userId: String, initialPoints: Double) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        val userData = hashMapOf(
            "userId" to userId,
            "points" to initialPoints // Store as a Double (or Float)
        )
        userRef.set(userData)
            .addOnSuccessListener {
                // Document created successfully
                Log.d(tag, "User document created with ID: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error creating user document", e)
                // Handle the error appropriately
            }
    }
    private fun updatePointsInDatabase(userId: String, newPoints: Double) { // Changed to Double
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("points", newPoints)
            .addOnSuccessListener { Log.d("MainActivity", "Points updated successfully") }
            .addOnFailureListener { e -> Log.e("MainActivity", "Error updating points", e) }
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
