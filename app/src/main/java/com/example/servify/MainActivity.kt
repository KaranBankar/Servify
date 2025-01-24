package com.example.servify

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.e("Firebase", "Error initializing Firebase", e)
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
    }

}