package com.example.smkituidemoapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ExerciseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_list)

        val exerciseListView = findViewById<ListView>(R.id.exerciseListView)
        val startWorkoutButton = findViewById<Button>(R.id.startWorkoutButton)

        val exercises = intent.getStringArrayListExtra("exercises") ?: arrayListOf()
        val workoutId = intent.getStringExtra("workoutId")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exercises)
        exerciseListView.adapter = adapter

        startWorkoutButton.setOnClickListener {
            setResult(RESULT_OK, intent.putExtra("workoutId", workoutId))
            finish()
        }
    }
}
