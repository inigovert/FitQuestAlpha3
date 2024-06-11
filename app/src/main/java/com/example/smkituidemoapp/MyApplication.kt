package com.example.smkituidemoapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions // Import FirebaseOptions

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Manual Firebase Initialization (If you don't have google-services.json)
        val firebaseOptions = FirebaseOptions.Builder()
            .setProjectId("fitquest-3ea1c") // Replace with your project ID
            .setApplicationId("1:32473756709:android:4bafd95e327841fb4afbcc")  // Replace with your app ID
            .setApiKey("AIzaSyBFl1TXH0HL7PbLhCd-qXgQcwLZGS5EkOQ")       // Replace with your API key
            .build()
        FirebaseApp.initializeApp(this, firebaseOptions)
    }
}
