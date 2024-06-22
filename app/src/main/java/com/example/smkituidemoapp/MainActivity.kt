package com.example.smkituidemoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.sency.smbase.core.listener.ConfigurationResult
import com.sency.smkitui.SMKitUI
import com.sency.smkitui.listener.SMKitUIWorkoutListener
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.WorkoutSummaryData
import com.sency.smkitui.model.SMExercise

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

    private val WORKOUT = 101

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()
        setClickListeners()
    }

    private fun startWorkoutForExercise(exercise: SMExercise) {
        smKitUI?.let {
            val workout = SMWorkout(
                id = exercise.prettyName.lowercase().replace(" ", "_") + "_workout",
                name = "${exercise.prettyName} Workout",
                workoutIntro = "",
                soundtrack = "soundtrack_7",
                exercises = listOf(exercise),
                workoutClosure = "workoutClosure.mp3",
                getInFrame = "bodycal_get_in_frame",
                bodycalFinished = "bodycal_finished"
            )
            it.startWorkout(workout, this)
        }
    }

    private fun setClickListeners() {
        binding.startAssessment.setOnClickListener {
            smKitUI?.startAssessment(this)
        }
        binding.startCustomWorkout.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "50",
                    name = "Demo Workout",
                    workoutIntro = "",
                    soundtrack = "soundtrack_7",
                    exercises = viewModel.exercises(),
                    workoutClosure = "workoutClosure.mp3",
                    getInFrame = "bodycal_get_in_frame",
                    bodycalFinished = "bodycal_finished"
                )
                it.startWorkout(smWorkout, this)
            }
        }
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bmiCalculatorButton.setOnClickListener {
            startActivity(Intent(this, BMICalculatorActivity::class.java))
        }
        binding.chooseWorkoutButton.setOnClickListener {
            val intent = Intent(this, ChooseWorkoutActivity::class.java)
            startActivityForResult(intent, WORKOUT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == WORKOUT && resultCode == RESULT_OK) {
            val selectedExerciseIndex = data?.getIntExtra("selectedExerciseIndex", -1) ?: -1
            if (selectedExerciseIndex != -1) {
                startWorkoutForExercise(viewModel.exercises()[selectedExerciseIndex])
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
    }

    override fun handleWorkoutErrors(error: Error) {
        Log.d(tag, "handleWorkoutErrors: $error")
    }

    override fun workoutDidFinish(summary: WorkoutSummaryData) {
        Log.d(tag, "workoutDidFinish: $summary")

        // Extract and display exercise scores
        val exercises = summary.exercises
        for (exercise in exercises) {
            val exerciseScore = exercise.totalScore
            Log.d(tag, "Exercise ${exercise.prettyName}: Score = $exerciseScore")
        }

        // Calculate and display points using extracted scores
        val totalExercises = summary.exercises.size
        var totalScore = 0f

        for (exercise in summary.exercises) {
            totalScore += exercise.totalScore // Use exercise.totalScore
        }

        val averageScore = if (totalExercises > 0) totalScore / totalExercises else 0f
        val points = calculatePoints(averageScore)

        // Update viewModel
        viewModel.updateExercisePoints(points)

        // Update UI to display the calculated points
        binding.pointsTextView.text = "Points: $points"
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
