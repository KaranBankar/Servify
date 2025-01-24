package com.example.servify

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.e("Firebase", "Error initializing Firebase", e)
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
    }

}