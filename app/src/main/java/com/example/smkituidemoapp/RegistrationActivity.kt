package com.example.smkituidemoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smkituidemoapp.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

data class Gym(
    val Name: String = "",
    val Location: String = "",
    val id: String = "" // Add this field to store the gym's document ID
)

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gymSpinner: Spinner
    private lateinit var gyms: List<Gym>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseFirestore.setLoggingEnabled(true)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        gymSpinner = binding.Gymspinner // Ensure your spinner ID matches this

        fetchGyms()

        binding.registerButton.setOnClickListener {
            val firstName = binding.firstNameTextInput.text.toString().trim()
            val lastName = binding.lastNameTextInput.text.toString().trim()
            val email = binding.emailTextInput.text.toString().trim()
            val password = binding.passwordTextInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordTextInput.text.toString().trim()
            val selectedGym = gyms[gymSpinner.selectedItemPosition]

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                generateMemberIdAndCreateAccount(firstName, lastName, email, password, selectedGym)
            }
        }
    }

    private fun fetchGyms() {
        db.collection("Gym").get()
            .addOnSuccessListener { result ->
                gyms = result.map {
                    val gym = it.toObject<Gym>()
                    gym.copy(id = it.id) // Set the document ID
                }
                val gymNames = gyms.map { "${it.Name}, ${it.Location}" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gymNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                gymSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch gyms. ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateMemberIdAndCreateAccount(firstName: String, lastName: String, email: String, password: String, gym: Gym) {
        val memberRef = db.collection("Gym").document(gym.id).collection("Members")

        memberRef.get().addOnSuccessListener { result ->
            val existingIds = result.map { it.id }
            val newId = generateNextId(existingIds)

            createAccount(firstName, lastName, email, password, gym, newId)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to generate member ID. ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateNextId(existingIds: List<String>): String {
        val prefix = "U"
        val maxId = existingIds
            .filter { it.startsWith(prefix) }
            .mapNotNull { it.removePrefix(prefix).toIntOrNull() }
            .maxOrNull() ?: 0
        return "$prefix${String.format("%03d", maxId + 1)}"
    }

    private fun createAccount(firstName: String, lastName: String, email: String, password: String, gym: Gym, memberId: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateProfileAndSaveData(user, firstName, lastName, email, gym, memberId)
                    } else {
                        Toast.makeText(this, "User authentication failed.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("createAccount", "Registration failed: ", task.exception)
                    Toast.makeText(this, "Registration Failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateProfileAndSaveData(user: FirebaseUser, firstName: String, lastName: String, email: String, gym: Gym, memberId: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("$firstName $lastName")
            .build()
        user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
            if (profileTask.isSuccessful) {
                saveMemberData(user.uid, firstName, lastName, email, gym, memberId)
            } else {
                Log.e("createAccount", "Failed to update user profile: ", profileTask.exception)
                Toast.makeText(this, "Failed to update user profile.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveMemberData(uid: String, firstName: String, lastName: String, email: String, gym: Gym, memberId: String) {
        val memberRef = db.collection("Gym").document(gym.id)
            .collection("Members").document(memberId)
        val userData = hashMapOf(
            "UID" to uid,
            "First Name" to firstName,
            "Last Name" to lastName,
            "Email" to email,
            "Status" to "Active User"
        )
        memberRef.set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("createAccount", "Failed to store member data in Firestore: ", e)
                Toast.makeText(this, "Failed to store member data in Firestore.", Toast.LENGTH_LONG).show()
            }
    }

}
