package com.example.servify

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ServiceProviderAdapter(private var serviceProviders: List<User1>) :
    RecyclerView.Adapter<ServiceProviderAdapter.ServiceProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceProviderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_provider, parent, false)
        return ServiceProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceProviderViewHolder, position: Int) {
        val user = serviceProviders[position]

        holder.tvName.text = "Owner : ${user.name}"
        holder.tvBusinessTitle.text = "Service : ${user.businessTitle}"
        holder.tvBusinessDescription.text = user.businessDescription
        holder.tvMobile.text = "Mobile: ${user.mobile}"
        holder.tvEmail.text = "Email: ${user.email}"
        holder.tvAddress.text = "Address: ${user.address}"
        holder.price.text="Price : ${user.price}"

        // Decoding the Base64 image
        try {
            val decodedBytes = android.util.Base64.decode(user.image, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                holder.ivProfile.setImageBitmap(bitmap)
            } else {
                holder.ivProfile.setImageResource(R.drawable.servifylofo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.ivProfile.setImageResource(R.drawable.servifylofo)
        }

        // Setting up onClickListener for the button
        holder.btnBook.setOnClickListener {
            // Create an intent to navigate to BookingActivity
            val intent = Intent(holder.itemView.context, BookingActivity::class.java)

            // Pass the data to BookingActivity
            intent.putExtra("name", user.name)
            intent.putExtra("businessTitle", user.businessTitle)
            intent.putExtra("businessDescription", user.businessDescription)
            intent.putExtra("mobile", user.mobile)
            intent.putExtra("email", user.email)
            intent.putExtra("address", user.address)
            intent.putExtra("image", user.image)
            intent.putExtra("price",user.price)// Pass the Base64 image string

            // Start the BookingActivity
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = serviceProviders.size

    fun updateData(newServiceProviders: List<User1>) {
        (serviceProviders as MutableList).apply {
            clear()
            addAll(newServiceProviders)
        }
        notifyDataSetChanged()
    }

    inner class ServiceProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvBusinessTitle: TextView = itemView.findViewById(R.id.tvBusinessTitle)
        val tvBusinessDescription: TextView = itemView.findViewById(R.id.tvBusinessDescription)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile) // Added
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)   // Added
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val btnBook: TextView= itemView.findViewById(R.id.btnBook)
    val price:TextView=itemView.findViewById(R.id.tvPrice)// Button to book service
    }
}
