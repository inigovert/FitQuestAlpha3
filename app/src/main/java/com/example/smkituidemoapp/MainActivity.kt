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
import com.sency.smbase.core.listener.ConfigurationResult
import com.sency.smkitui.BuildConfig
import com.sency.smkitui.SMKitUI
import com.sency.smkitui.listener.SMKitUIWorkoutListener
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.WorkoutSummaryData
import com.sency.smkitui.model.SMExercise

class MainActivity : AppCompatActivity(), SMKitUIWorkoutListener {

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
            id = exercise.name.toLowerCase().replace(" ", "_") + "_workout",
            name = "${exercise.name} Workout",
            workoutIntro = Uri.EMPTY,
            soundtrack = Uri.EMPTY,
            exercises = listOf(exercise), // List with the single selected exercise
            workoutClosure = Uri.EMPTY
        )
        smKitUI?.startWorkout(workout, this)
    }

    private fun setClickListeners() {
        binding.startAssessment.setOnClickListener {
            smKitUI?.startAssessment(this)
        }
        binding.startCustomWorkout.setOnClickListener {
            smKitUI?.let {
                val smWorkout = SMWorkout(
                    id = "",
                    name = "TEST",
                    workoutIntro = Uri.EMPTY,
                    soundtrack = Uri.EMPTY,
                    exercises = viewModel.exercises(),
                    workoutClosure = Uri.EMPTY
                )
                it.startWorkout(smWorkout, this)
            }
        }
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.squatButton.setOnClickListener {
            startWorkoutForExercise(viewModel.exercises()[1]) // Squat is at index 1
        }

        binding.bmiCalculatorButton.setOnClickListener {
            startActivity(Intent(this, BMICalculatorActivity::class.java))
        }
        binding.chooseWorkoutButton.setOnClickListener {
            startActivity(Intent(this, chooseWorkoutActivity::class.java))
        }
    }


    private fun observeConfiguration() {
        viewModel.configured.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.INVISIBLE
                binding.startAssessment.visibility = View.VISIBLE
                binding.startCustomWorkout.visibility = View.VISIBLE
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
