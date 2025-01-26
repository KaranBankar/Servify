package com.example.servify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servify.databinding.ActivityReauestedOrdersBinding
import com.google.firebase.database.*

// This is your ReauestedOrdersActivity class
class ReauestedOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReauestedOrdersBinding
    private lateinit var database: DatabaseReference
    private val ordersList = mutableListOf<Orders1>() // List to hold orders
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReauestedOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("orders")

        // Set up RecyclerView
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(this, ordersList)
        binding.ordersRecyclerView.adapter = ordersAdapter

        // Show loading spinner initially
        binding.progressBar.visibility = View.VISIBLE

        // Fetch orders
        fetchOrders()
    }

    private fun fetchOrders() {
        // Assuming you want to fetch orders by user contact
        val sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        val userContact = sharedPreferences.getString("mobile", "") // "" is the default if not found

        Toast.makeText(this, "User Mobile: $userContact", Toast.LENGTH_SHORT).show()
          // Replace with actual user contact from your app
        database.orderByChild("serviceProviderContact").equalTo(userContact).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordersList.clear() // Clear the list to avoid duplicates
                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val orderId = orderSnapshot.key // The orderId is the key of each order
                        val order = orderSnapshot.getValue(Orders1::class.java)
                        if (order != null) {
                            order.id = orderId // Assign the orderId to the order object
                            ordersList.add(order)
                        }
                    }
                    ordersAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ReauestedOrdersActivity, "No orders found", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ReauestedOrdersActivity, "Failed to fetch orders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
