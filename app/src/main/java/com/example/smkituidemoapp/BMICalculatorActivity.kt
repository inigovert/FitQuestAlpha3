package com.example.smkituidemoapp


import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BMICalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        val heightInput: EditText = findViewById(R.id.heightTextInput)
        val weightInput: EditText = findViewById(R.id.weightTextInput)
        val resultText: TextView =
            findViewById(R.id.resultText) // Add a TextView in your layout for the result
        val calculateButton: Button =
            findViewById(R.id.calculateButton) // Add a Button in your layout

        calculateButton.setOnClickListener {

            val heightStr = heightInput.text.toString()
            val weightStr = weightInput.text.toString()

            if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {

                val result = calculateBMI(heightStr.toDouble(), weightStr.toDouble())
                resultText.text = "Your BMI: ${
                    String.format(
                        "%.2f",
                        result.bmi
                    )
                }\nClassification: ${result.classification}"
            } else {
                resultText.text = "Please enter your height and weight."
            }
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
}