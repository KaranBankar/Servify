package com.example.servify

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servify.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                Toast.makeText(this, "All inputs are valid. Proceed with sign-up logic.", Toast.LENGTH_SHORT).show()
                // Proceed with sign-up logic here
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateInputs(): Boolean {


        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()




        if (mobile.isEmpty() || !mobile.matches(Regex("^\\d{10}$"))) {
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
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=]).{8,}\$")
        return password.matches(passwordPattern)
    }
}