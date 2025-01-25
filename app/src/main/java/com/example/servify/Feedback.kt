package com.example.servify

import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Feedback : AppCompatActivity() {
    private lateinit var etFeedback: EditText
    private lateinit var btnFeedback: MaterialCardView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        database = Firebase.database.reference.child("feedback")

        // Initialize Views
        etFeedback = findViewById(R.id.etFeedback)
        btnFeedback = findViewById(R.id.btnFeedback)

        btnFeedback.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val feedbackText = etFeedback.text.toString().trim()

        // Validation
        if (TextUtils.isEmpty(feedbackText)) {
            etFeedback.error = "Feedback cannot be empty"
            return
        }

        if (feedbackText.length < 10) {
            etFeedback.error = "Feedback must be at least 10 characters"
            return
        }

        // Show Progress Dialog
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Submitting feedback...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // Create a unique ID for the feedback
        val feedbackId = database.push().key ?: return

        // Store feedback in Firebase
        val feedbackData = mapOf(
            "id" to feedbackId,
            "feedback" to feedbackText
        )
        database.child(feedbackId).setValue(feedbackData)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                    etFeedback.text.clear() // Clear the feedback input
                } else {
                    Toast.makeText(this, "Failed to submit feedback. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
