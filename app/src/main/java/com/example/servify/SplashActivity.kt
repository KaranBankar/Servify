package com.example.servify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        // Hold the splash screen for 1 second, then navigate to the Login screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish SplashActivity so it's removed from the back stack
        }, 1000) // Delay in milliseconds (1000ms = 1 second)
    }
}
