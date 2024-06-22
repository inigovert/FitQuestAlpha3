package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.viewModels.MainViewModel

class ChooseWorkoutActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_workout)

        val layout = findViewById<LinearLayout>(R.id.workoutButtonLayout)
        val exercises = viewModel.exercises()

        for ((index, exercise) in exercises.withIndex()) {
            val button = Button(this).apply {
                text = exercise.prettyName
                setOnClickListener {
                    val intent = Intent().apply {
                        putExtra("selectedExerciseIndex", index)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            layout.addView(button)
        }
    }
}
