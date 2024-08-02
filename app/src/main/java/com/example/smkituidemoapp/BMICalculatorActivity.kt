package com.example.smkituidemoapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityBmiCalculatorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BMICalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiCalculatorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var dateInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        heightInput = findViewById(R.id.heightTextInput)
        weightInput = findViewById(R.id.weightTextInput)
        dateInput = findViewById(R.id.dateInput)

        val resultText: TextView = findViewById(R.id.resultText)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val logWeightButton: Button = findViewById(R.id.logWeightButton)

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.itemIconTintList = null

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.profileFragment -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.rewardsFragment -> {
                    val currentUser = auth.currentUser
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

        calculateButton.setOnClickListener {
            val heightStr = heightInput.text.toString()
            val weightStr = weightInput.text.toString()

            if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
                val result = calculateBMI(heightStr.toDouble(), weightStr.toDouble())
                resultText.text = "Your BMI: ${String.format("%.2f", result.bmi)}\nClassification: ${result.classification}"
            } else {
                resultText.text = "Please enter your height and weight."
            }
        }

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        logWeightButton.setOnClickListener {
            logWeight()
        }
    }

    private data class BMIResult(val bmi: Double, val classification: String)

    private fun calculateBMI(heightCm: Double, weightKg: Double): BMIResult {
        val heightMeters = heightCm / 100.0
        val bmi = weightKg / (heightMeters * heightMeters)

        val classification = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Healthy"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }

        return BMIResult(bmi, classification)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateInput.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun logWeight() {
        val weightStr = weightInput.text.toString()
        val dateStr = dateInput.text.toString()

        if (weightStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please enter weight and date", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        if (weight == null) {
            Toast.makeText(this, "Invalid weight value", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)
        val currentUser = auth.currentUser

        if (currentUser != null && date != null) {
            val weightEntry = hashMapOf(
                "weight" to weight,
                "date" to date,
                "bmi" to calculateBMI(heightInput.text.toString().toDouble(), weight).bmi
            )

            db.collection("users").document(currentUser.uid).collection("weight_entries")
                .add(weightEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "Weight logged successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error logging weight: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
