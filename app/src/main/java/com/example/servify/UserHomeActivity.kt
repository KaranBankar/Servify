package com.example.servify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.servify.databinding.ActivitySerUserSignupBinding
import com.example.servify.databinding.ActivityUserHomeBinding
import com.google.firebase.database.FirebaseDatabase

class UserHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceProviderAdapter: ServiceProviderAdapter
    lateinit var binding: ActivityUserHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set padding for system bars (status bar, navigation bar)
        applyWindowInsets()

        setupToolbar()
        setupNavigationDrawer()

        // Initialize RecyclerView and adapter once
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
            // Apply system bar insets as padding
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Required to propagate insets further
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Initializes the RecyclerView with the adapter and layout manager.
     */
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)

        // Initialize adapter with an empty list as default
        serviceProviderAdapter = ServiceProviderAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = serviceProviderAdapter
    }

    /**
     * Fetches the list of service providers from Firebase Realtime Database and updates the RecyclerView.
     */
    private fun fetchServiceProviders() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Start fetching data from Firebase
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val serviceProviders = mutableListOf<User1>()

                // Loop through the fetched data and filter out relevant service providers
                for (child in snapshot.children) {
                    val user = child.getValue(User1::class.java)
                    if (user != null && user.businessTitle.isNotEmpty() && user.businessDescription.isNotEmpty()) {
                        serviceProviders.add(user)
                    }
                }

                // Update the RecyclerView adapter with the fetched data
                serviceProviderAdapter.updateData(serviceProviders)
            }
            .addOnFailureListener { exception ->
                // Show a toast message if data fetch fails
                Toast.makeText(this, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
