package com.example.servify

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servify.databinding.ActivityEditServiceBinding
import com.example.servify.databinding.ActivityServiceProviderHomeBinding

class ServiceProviderHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceProviderHomeBinding
    private val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityServiceProviderHomeBinding.inflate(layoutInflater)
        binding.layoutEditOrders.setOnClickListener {
            val intent = Intent(this@ServiceProviderHomeActivity, EditServiceActivity::class.java)
            startActivity(intent)

        }
        binding.layoutRequestedOrders.setOnClickListener {
            val intent = Intent(this@ServiceProviderHomeActivity, ReauestedOrdersActivity::class.java)
            startActivity(intent)

        }
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}