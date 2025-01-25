package com.example.servify

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servify.databinding.ActivityLoginBinding
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "LoginPreferences"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_TYPE = "userType"
        private const val KEY_MOBILE = "mobile"
        private const val KEY_PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Check login status
        if (isLoggedIn()) {
            navigateToHome()
            return
        }
        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Logging in...")
            setCancelable(false)
        }

        binding.tvServiceProSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, SerproSignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvServiceUserSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, SerUserSignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUser() {
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        progressDialog.show()

        // Query the database to find the user with the given mobile number
        databaseReference.child(mobile)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    if (snapshot.exists()) {
                        val dbPassword = snapshot.child("password").value as? String
                        val businessTitle = snapshot.child("businessTitle").value as? String

                        if (dbPassword == password) {
                            val userType = if (!businessTitle.isNullOrEmpty()) "business" else "normal"
                            saveLoginDetails(mobile, password, userType)
                            navigateToHome()
                        } else {
                            Toast.makeText(this@LoginActivity, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "No user found with this mobile number.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Error fetching user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveLoginDetails(mobile: String, password: String, userType: String) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_MOBILE, mobile)
            putString(KEY_PASSWORD, password)
            putString(KEY_USER_TYPE, userType)
            apply()
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun navigateToHome() {
        val userType = sharedPreferences.getString(KEY_USER_TYPE, null)
        val intent = if (userType == "business") {
            Intent(this, ServiceProviderHomeActivity::class.java)
        } else {
            Intent(this, UserHomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun validateInputs(): Boolean {
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (mobile.isEmpty() || !mobile.matches(Regex("^\\d{10}\$"))) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number."
            return false
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            binding.etPassword.error = "Password must be at least 8 characters with 1 uppercase, 1 number, and 1 special character."
            return false
        }

        return true
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\\$%^&+=]).{8,}\$")
        return password.matches(passwordPattern)
    }
}
