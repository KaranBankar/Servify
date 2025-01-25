package com.example.servify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servify.databinding.ActivityServiceProviderHomeBinding

class ServiceProviderHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceProviderHomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityServiceProviderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString(LoginActivity.KEY_USER_NAME, "User")

        // Set the toolbar title as "Hi, $name"
        setupToolbar(userName)
        setupNavigationDrawer()

        // OnClickListener for EditService
        binding.layoutEditOrders.setOnClickListener {
            val intent = Intent(this@ServiceProviderHomeActivity, EditServiceActivity::class.java)
            startActivity(intent)
        }

        // OnClickListener for RequestedOrders
        binding.layoutRequestedOrders.setOnClickListener {
            val intent = Intent(this@ServiceProviderHomeActivity, ReauestedOrdersActivity::class.java)
            startActivity(intent)
        }

        // Edge-to-Edge padding setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Toolbar setup
    private fun setupToolbar(userName: String?) {
        setSupportActionBar(binding.toolbar)

        // Set the title of the toolbar to "Hi, $name"
        supportActionBar?.title = "Hi, ${userName ?: "User"}" // Use "User" as default if name is null

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Add drawer toggle for navigation
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    // Navigation Drawer setup
    private fun setupNavigationDrawer() {
        // Fetching the saved details from SharedPreferences
        val userName = sharedPreferences.getString(LoginActivity.KEY_USER_NAME, "User")
        val userEmail = sharedPreferences.getString(LoginActivity.KEY_USER_EMAIL, "user@example.com")
        val userImageBase64 = sharedPreferences.getString(LoginActivity.KEY_USER_IMAGE, null)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Handle Home click (optional, e.g., navigate to the home screen or refresh the current screen)
                    //Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_feedback -> {
                    val intent = Intent(this@ServiceProviderHomeActivity, Feedback::class.java)
                    startActivity(intent)


                    // Handle Feedback click
                    //Toast.makeText(this, "Feedback clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_share -> {
                    // Share app on WhatsApp
                    shareAppOnWhatsApp()
                }
                R.id.nav_logout -> {
                    // Logout with confirmation
                    confirmLogout()
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)

            // Set the user details in the navigation drawer header
            val headerView = binding.navView.getHeaderView(0)
            val imageView: ImageView = headerView.findViewById(R.id.nav_user_image)
            val nameTextView: TextView = headerView.findViewById(R.id.nav_user_name)
            val emailTextView: TextView = headerView.findViewById(R.id.nav_user_email)

            // Set the name and email
            nameTextView.text = userName
            emailTextView.text = userEmail

            // Decode and display the user's profile image if available
            if (!userImageBase64.isNullOrEmpty()) {
                try {
                    // Decode the Base64 string and set the image
                    val decodedBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    imageView.setImageResource(R.drawable.avatr)
                } catch (e: Exception) {
                         imageView.setImageResource(R.drawable.avatr) // Default image in case of error
                }
            } else {
                imageView.setImageResource(R.drawable.avatr) // Default image if no Base64 string is found
            }

            true
        }
    }

    // Method to refresh navigation drawer after login or image update
    private fun refreshNavigationDrawer() {
        val userName = sharedPreferences.getString(LoginActivity.KEY_USER_NAME, "User")
        val userEmail = sharedPreferences.getString(LoginActivity.KEY_USER_EMAIL, "user@example.com")
        val userImageBase64 = sharedPreferences.getString(LoginActivity.KEY_USER_IMAGE, null)

        // Show Toasts for the refreshed values

        // Log the refreshed values to check if they are being fetched correctly

        // Force the navigation drawer to update with the new image and details
        val headerView = binding.navView.getHeaderView(0)
        val imageView: ImageView = headerView.findViewById(R.id.nav_user_image)
        val nameTextView: TextView = headerView.findViewById(R.id.nav_user_name)
        val emailTextView: TextView = headerView.findViewById(R.id.nav_user_email)

        nameTextView.text = userName
        emailTextView.text = userEmail

        // Show Toasts for the values being set in the UI components

        if (!userImageBase64.isNullOrEmpty()) {
            try {
                // Decode the Base64 string and set the image
                val decodedBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageResource(R.drawable.avatr)
            } catch (e: Exception) {
                Log.e("ServiceProviderHome", "Error decoding Base64 image", e)
                imageView.setImageResource(R.drawable.avatr) // Default image in case of error
                Toast.makeText(this, "Error decoding Base64 image", Toast.LENGTH_SHORT).show()
            }
        } else {
            imageView.setImageResource(R.drawable.avatr) // Default image if no Base64 string is found
           // Toast.makeText(this, "Using default image", Toast.LENGTH_SHORT).show()
        }

        // Force the navigation drawer to invalidate and refresh the header view
        binding.navView.getHeaderView(0).invalidate()
    }

    override fun onStart() {
        super.onStart()

            refreshNavigationDrawer()

    }
    private fun shareAppOnWhatsApp() {
        val shareMessage = "Check out Servify App to connect with service providers near you! Download now: https://play.google.com/store/apps/details?id=com.example.servify"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                setPackage("com.whatsapp") // Ensures it opens in WhatsApp
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun confirmLogout() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Clear login preferences and navigate to LoginActivity
            sharedPreferences.edit().clear().apply()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close the current activity
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close the dialog if No is clicked
        }
        builder.create().show()
    }
}
