package com.example.servify

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(
    private val context: Context,
    private val orders: List<Orders1>  // Ensure Orders1 is the correct model class
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvServiceName: TextView = itemView.findViewById(R.id.tvBusinessTitle)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserMobile: TextView = itemView.findViewById(R.id.tvUserMobile)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        val btnMarkAsComplete: TextView = itemView.findViewById(R.id.btnStatus)
        var cardview: CardView =itemView.findViewById(R.id.carviewSnap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_provider_order_received, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvServiceName.text = order.serviceName ?: "N/A"
        holder.tvUserName.text = order.userName ?: "N/A"
        holder.tvUserMobile.text = "Mobile: ${order.userContact ?: "N/A"}"
        holder.tvAddress.text = "Address: ${order.userAddress ?: "N/A"}"
        holder.tvPaymentStatus.text = when (order.paymentMethod) {
            "online" -> "Payment UTR: ${order.utr ?: "N/A"}"
            else -> "Cash on Delivery"
        }

        if (order.completionStatus == "Completed") {
            holder.btnMarkAsComplete.text = "Completed"
            holder.btnMarkAsComplete.isEnabled = false

            holder.cardview.setCardBackgroundColor(context.getColor(R.color.green))
            holder.cardview.backgroundTintList = context.getColorStateList(R.color.green)
        } else {
            holder.btnMarkAsComplete.text = "Mark As Complete"
            holder.btnMarkAsComplete.isEnabled = true
        }

        holder.btnMarkAsComplete.setOnClickListener {
            val intent = Intent(context, CompleteOrderActivity::class.java)
            intent.putExtra("orderId", order.id ?: "")
            intent.putExtra("userContact", order.userContact ?: "")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}
