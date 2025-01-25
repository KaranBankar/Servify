package com.example.servify

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.servify.databinding.ActivityBookingBinding
import com.google.firebase.database.FirebaseDatabase

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding

    companion object {
        private const val PREF_NAME = "LoginPreferences"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_MOBILE = "mobile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the data passed from ServiceProviderAdapter via Intent
        val name = intent.getStringExtra("name") ?: "Owner: Not available"
        val businessTitle = intent.getStringExtra("businessTitle") ?: "Service: Not available"
        val businessDescription = intent.getStringExtra("businessDescription") ?: "Description: Not available"
        val mobile = intent.getStringExtra("mobile") ?: "Mobile: Not available"
        val email = intent.getStringExtra("email") ?: "Email: Not available"
        val address = intent.getStringExtra("address") ?: "Address: Not available"
        val image = intent.getStringExtra("image") ?: "default"
        val price = intent.getStringExtra("price") ?: "Displayed Soon"

        // Set the values to the UI components
        binding.tvOwener.text = "Owner: $name"
        binding.tvServiceName.text = "Service: $businessTitle"
        binding.tvDescription.text = businessDescription
        binding.tvContact.text = "Contact: $mobile"
        binding.tvAddress.text = address
        binding.tvPrice.text = price

        // Check if image is passed, decode and display it, otherwise use default
        if (image != "default") {
            try {
                val decodedBytes = Base64.decode(image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                binding.imgServiceMedia.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imgServiceMedia.setImageResource(R.drawable.booknow) // Default image
            }
        } else {
            binding.imgServiceMedia.setImageResource(R.drawable.booknow) // Default image
        }

        // Initially hide the PhonePe options
        binding.layoutPayment.visibility = View.GONE

        // Cash on Delivery checkbox listener
        binding.cbCashOnDelivery.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Hide PhonePe options when Cash on Delivery is checked
                binding.layoutPayment.visibility = View.GONE
                binding.cbPhonePe.isChecked = false
            }
        }

        // PhonePe checkbox listener
        binding.cbPhonePe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show PhonePe options when PhonePe is checked
                binding.layoutPayment.visibility = View.VISIBLE
                // Hide Cash on Delivery options
                binding.cbCashOnDelivery.isChecked = false
            } else {
                // Hide PhonePe options when unchecked
                binding.layoutPayment.visibility = View.GONE
            }
        }

        // PhonePe button click listener
        binding.btnPhonePay.setOnClickListener {
            initiatePhonePePayment("9322067937@ybl", binding.tvPrice.text.toString())
        }

        // Confirm button click listener
        binding.btnConfirm.setOnClickListener {
            confirmOrder()
        }
    }

    private fun initiatePhonePePayment(upiId: String, amountText: String) {
        // Validate and parse the amount
        val amount: String = try {
            amountText.toInt().toString() // Ensure it's a valid integer
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            "100" // Default to ₹100 if conversion fails
        }

        // Construct the UPI URI
        val upiUri = android.net.Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId) // Payee UPI ID
            .appendQueryParameter("pn", "Service Provider") // Payee Name
            .appendQueryParameter("am", amount) // Payment Amount
            .appendQueryParameter("cu", "INR") // Currency
            .build()

        // Create an intent to open the UPI app
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = upiUri

        try {
            // Try to launch the UPI app
            startActivityForResult(intent, 1)
        } catch (e: android.content.ActivityNotFoundException) {
            // If no UPI app is installed, show an error message
            Toast.makeText(this, "No UPI app found. Please install PhonePe or another UPI-compatible app.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmOrder() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Retrieve user data from SharedPreferences
        val userName = sharedPreferences.getString(KEY_USER_NAME, "Unknown User")
        val userAddress = sharedPreferences.getString(KEY_USER_EMAIL, "Unknown Address")
        val userContact = sharedPreferences.getString(KEY_MOBILE, "Unknown Contact")

        // Retrieve service provider data from Intent
        val serviceName = intent.getStringExtra("businessTitle") ?: "Unknown Service"
        val serviceAddress = intent.getStringExtra("address") ?: "Unknown Address"
        val serviceProviderContact = intent.getStringExtra("mobile") ?: "Unknown Contact"
        val price = binding.tvPrice.text.toString()

        // Validate the selected payment method
        val paymentMethod: String
        val utr: String? // Will be null if payment method is Cash
        if (binding.cbCashOnDelivery.isChecked) {
            paymentMethod = "cash"
            utr = null
        } else if (binding.cbPhonePe.isChecked) {
            utr = binding.etUtr.text.toString()
            if (utr.isEmpty()) {
                Toast.makeText(this, "Please enter a valid UTR number for online payment", Toast.LENGTH_SHORT).show()
                return
            }
            paymentMethod = "online"
        } else {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        // Construct the order object
        val order = hashMapOf(
            "serviceName" to serviceName,
            "serviceAddress" to serviceAddress,
            "userName" to userName,
            "userAddress" to userAddress,
            "userContact" to userContact,
            "serviceProviderContact" to serviceProviderContact,
            "price" to price,
            "completionStatus" to "Pending", // Default status
            "paymentMethod" to paymentMethod,
            "utr" to (utr ?: "cash")
        )

        // Store the order data in Firebase
        val database = FirebaseDatabase.getInstance()
        val ordersRef = database.getReference("orders")

        ordersRef.push().setValue(order)
            .addOnSuccessListener {
                val intent=Intent(this,UserHomeActivity::class.java)
                Toast.makeText(this, "Order confirmed successfully", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to confirm order. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
