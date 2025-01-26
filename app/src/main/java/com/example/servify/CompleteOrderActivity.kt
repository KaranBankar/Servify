package com.example.servify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class CompleteOrderActivity : AppCompatActivity() {

    private lateinit var orderId: String
    private lateinit var userContact: String
    private lateinit var generatedOtp: String
    private val REQUEST_SMS_PERMISSION = 1 // Permission request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_order)

        // Receive data from the Intent
        orderId = intent.getStringExtra("orderId") ?: ""
        userContact = intent.getStringExtra("userContact") ?: ""
        Toast.makeText(this,"Id:$orderId",Toast.LENGTH_SHORT).show()

        // Generate OTP
        generatedOtp = generateOtp()

        // Check SMS permission and send OTP
        checkSmsPermissionAndSendOtp()

        // Setup OTP verification
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val submitBtn = findViewById<TextView>(R.id.submitBtn)

        submitBtn.setOnClickListener {
            val enteredOtp = etOtp.text.toString()

            // Check OTP
            if (enteredOtp == generatedOtp) {
                updateOrderStatus(orderId)
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    private fun checkSmsPermissionAndSendOtp() {
        val permission = Manifest.permission.SEND_SMS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_SMS_PERMISSION)
        } else {
            // Permission granted, send OTP
            sendOtp(userContact, generatedOtp)
        }
    }

    private fun sendOtp(contact: String, otp: String) {
        val message = "Your OTP for order completion is: $otp"
        sendTextMessage(contact, message)
    }

    private fun sendTextMessage(contact: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(contact, null, message, null, null)
            Toast.makeText(this, "Message sent to $contact", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderStatus(orderId: String) {
        if (orderId.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            val ordersRef = database.getReference("orders").child(orderId)

            // Update the order's completion status in Firebase
            ordersRef.child("completionStatus").setValue("Completed")
                .addOnSuccessListener {
                    Toast.makeText(this, "Order marked as completed", Toast.LENGTH_SHORT).show()
                    finish()  // Close the activity after completing the order
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update order status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send OTP
                sendOtp(userContact, generatedOtp)
            } else {
                Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
