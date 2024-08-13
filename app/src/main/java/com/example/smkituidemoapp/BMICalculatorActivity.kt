package com.example.smkituidemoapp

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.MarkerView

class BMICalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiCalculatorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        lineChart = findViewById(R.id.lineChart)
        heightInput = findViewById(R.id.heightTextInput)
        weightInput = findViewById(R.id.weightTextInput)
        dateInput = findViewById(R.id.dateInput)
        binding.dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        fetchAndDisplayWeightData()

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
                resultText.text = "Your BMI: ${result.bmi}\nClassification: ${result.classification}"
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

    private data class BMIResult(val bmi: Int, val classification: String)

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun onDateSelected(date: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        binding.dateInput.setText(dateString)
        Toast.makeText(this, "Selected date: $dateString", Toast.LENGTH_SHORT).show()
    }

    private fun fetchAndDisplayWeightData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchGymIdAndMemberId { gymId, memberId ->
                if (gymId != null && memberId != null) {
                    db.collection("Gym").document(gymId).collection("Members").document(memberId)
                        .collection("weight_entries")
                        .get()
                        .addOnSuccessListener { documents ->
                            val weightEntries = mutableListOf<Entry>()
                            for (document in documents) {
                                val date = document.getDate("date")
                                val weight = document.getDouble("weight")
                                if (date != null && weight != null) {
                                    val entry = Entry(date.time.toFloat(), weight.toFloat())
                                    weightEntries.add(entry)
                                }
                            }
                            plotWeightData(weightEntries)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error fetching weight data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Gym ID or Member ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }



    private fun plotWeightData(entries: List<Entry>) {
        if (entries.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No weight data available")
            lineChart.invalidate()
            return
        }

        val dataSet = LineDataSet(entries, "Weight")
        dataSet.color = Color.WHITE // Line color
        dataSet.valueTextColor = Color.WHITE // Value (text) color
        dataSet.setCircleColor(Color.WHITE) // Circle color
        dataSet.setDrawCircleHole(false)
        dataSet.lineWidth = 2f // Line width
        dataSet.circleRadius = 3f // Circle radius

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Set the custom marker view
        val markerView = CustomMarkerView(this, R.layout.custom_marker_view)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        // Configure X-Axis
        val xAxis: XAxis = lineChart.xAxis
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Configure Y-Axis
        val yAxisLeft: YAxis = lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.setDrawGridLines(false)

        val yAxisRight: YAxis = lineChart.axisRight
        yAxisRight.isEnabled = false

        // Configure Legend
        val legend: Legend = lineChart.legend
        legend.textColor = Color.WHITE

        // Configure Description
        lineChart.description.text = "Weight Progress"
        lineChart.description.textColor = Color.WHITE

        // Additional settings for better visibility
        lineChart.setBackgroundColor(Color.BLACK)
        lineChart.setNoDataTextColor(Color.WHITE)
        lineChart.setNoDataText("No weight data available")
        lineChart.invalidate() // Refresh the chart
    }

    private fun calculateBMI(heightCm: Double, weightKg: Double): BMIResult {
        val heightMeters = heightCm / 100.0
        val bmi = (weightKg / (heightMeters * heightMeters)).toInt()

        val classification = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Healthy"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }

        return BMIResult(bmi, classification)
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
            val bmiResult = calculateBMI(heightInput.text.toString().toDouble(), weight)
            val weightEntry = hashMapOf(
                "weight" to weight,
                "date" to date,
                "time" to SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                "bmi" to bmiResult.bmi,
                "bmi_classification" to bmiResult.classification
            )

            fetchGymIdAndMemberId { gymId, memberId ->
                if (gymId != null && memberId != null) {
                    val userDocRef = db.collection("Gym").document(gymId).collection("Members").document(memberId)
                    val weightEntriesCollectionRef = userDocRef.collection("weight_entries")
                    val weightDetailsDocRef = weightEntriesCollectionRef.document(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date))

                    weightDetailsDocRef
                        .set(weightEntry)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Weight logged successfully", Toast.LENGTH_SHORT).show()
                            fetchAndDisplayWeightData()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error logging weight: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("BMICalculatorActivity", "Error logging weight", e)
                        }
                } else {
                    Toast.makeText(this, "Failed to log weight. Gym ID or Member ID not found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in or invalid date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchGymIdAndMemberId(callback: (String?, String?) -> Unit) {
        val email = getUserEmail()
        if (email != null) {
            db.collectionGroup("Members")
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.first()
                        val gymId = document.reference.parent.parent?.id
                        val memberId = document.id
                        callback(gymId, memberId)
                    } else {
                        Log.e("BMICalculatorActivity", "No such document")
                        callback(null, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BMICalculatorActivity", "Error fetching member document", e)
                    callback(null, null)
                }
        } else {
            callback(null, null)
        }
    }

    private fun getUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }
}
