package com.example.servify

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.servify.databinding.ActivityUserHomeBinding
import com.google.firebase.database.FirebaseDatabase

class UserHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceProviderAdapter: ServiceProviderAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: ProgressDialog
    lateinit var binding: ActivityUserHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString(LoginActivity.KEY_USER_NAME, "User")

        // Initialize progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading services...")
            setCancelable(false) // Prevent dismissal by accidental touch
        }

        // Set padding for system bars (status bar, navigation bar)
        applyWindowInsets()

        // Set up toolbar and navigation drawer
        setupToolbar(userName)
        setupNavigationDrawer()

        // Initialize RecyclerView and adapter
        setupRecyclerView()

        // Fetch and display service providers from Firebase
        fetchServiceProviders()
    }

    /**
     * Sets padding for system bars on the root view.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar(userName: String?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Hi, ${userName ?: "User"}" // Default to "User" if name is null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val userName = sharedPreferences.getString(LoginActivity.KEY_USER_NAME, "User")
        val userEmail = sharedPreferences.getString(LoginActivity.KEY_USER_EMAIL, "user@example.com")
        val userImageBase64 = sharedPreferences.getString(LoginActivity.KEY_USER_IMAGE, null)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Handle Home click (optional, e.g., navigate to the home screen or refresh the current screen)
                    Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_feedback -> {
                    val intent = Intent(this@UserHomeActivity, Feedback::class.java)
                    startActivity(intent)
                    // Handle Feedback click
                   // Toast.makeText(this, "Feedback clicked", Toast.LENGTH_SHORT).show()
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
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after the item is clicked
            true
        }

        // Set user details in the navigation drawer header
        val headerView = binding.navView.getHeaderView(0)
        val imageView: ImageView = headerView.findViewById(R.id.nav_user_image)
        val nameTextView: TextView = headerView.findViewById(R.id.nav_user_name)
        val emailTextView: TextView = headerView.findViewById(R.id.nav_user_email)

        nameTextView.text = userName
        emailTextView.text = userEmail

        if (!userImageBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageResource(R.drawable.avatr)
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.avatr)
                Toast.makeText(this, "Error decoding Base64 image", Toast.LENGTH_SHORT).show()
            }
        } else {
            imageView.setImageResource(R.drawable.avatr)
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        serviceProviderAdapter = ServiceProviderAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = serviceProviderAdapter
    }

    private fun fetchServiceProviders() {
        // Show the progress dialog
        progressDialog.show()

        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val serviceProviders = mutableListOf<User1>()
                for (child in snapshot.children) {
                    val user = child.getValue(User1::class.java)
                    if (user != null && user.businessTitle.isNotEmpty() && user.businessDescription.isNotEmpty()) {
                        serviceProviders.add(user)
                    }
                }
                serviceProviderAdapter.updateData(serviceProviders)

                // Dismiss the progress dialog
                progressDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                // Dismiss the progress dialog
                progressDialog.dismiss()

                Toast.makeText(this, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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
