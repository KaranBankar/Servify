package com.example.servify

import OrderAdapter
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.*

class UserTrackOrders : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private var orderList: MutableList<Order> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_track_orders)

        // Initialize RecyclerView and ProgressDialog
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pass 'this' to provide context to the adapter
        orderAdapter = OrderAdapter(orderList, this)  // Pass 'this' as context
        recyclerView.adapter = orderAdapter

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading orders...")
        progressDialog.setCancelable(false)

        // Get the user's mobile number from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        val userMobile = sharedPreferences.getString("mobile", "9322067938")

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("orders")

        // Show progress dialog while fetching data
        progressDialog.show()

        // Fetch orders for the specific user
        if (userMobile != null) {
            fetchOrdersForUser(userMobile)
        } else {
            progressDialog.dismiss()
        }
    }

    private fun fetchOrdersForUser(userMobile: String) {
        // Fetch orders from Firebase where userMobile matches
        databaseReference.orderByChild("userContact").equalTo(userMobile)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    orderList.clear()  // Clear previous data
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val order = snapshot.getValue(Order::class.java)
                            order?.let {
                                orderList.add(it)
                            }
                        }
                        orderAdapter.notifyDataSetChanged()  // Notify adapter that data has changed
                    } else {
                        // Handle case where no data is found
                        findViewById<TextView>(R.id.tvNoOrders).visibility = View.VISIBLE
                    }
                    // Dismiss the progress dialog after data is loaded
                    progressDialog.dismiss()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    progressDialog.dismiss()
                }
            })
    }
}
