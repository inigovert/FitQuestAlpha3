package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChooseWorkoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_workout)

        val squatButton = findViewById<Button>(R.id.squatBtn)

        squatButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("selectedExercise", "Squat Regular Static") // Pass exercise name
            setResult(RESULT_OK, intent)
            finish() // Close this activity
        }
    }
}
