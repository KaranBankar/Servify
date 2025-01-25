import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.servify.Order
import com.example.servify.R

class OrderAdapter(private val orderList: List<Order>, private val context: Context) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_user_track_orders, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        // Ensure you handle null values by providing fallback values
        holder.tvBusinessTitle.text = order.serviceName ?: "No Service Name"
        holder.tvName.text = order.userName ?: "No Name"
        holder.tvBusinessDescription.text = order.serviceAddress ?: "No Service Address"
        holder.tvMobile.text = "Mobile: " + (order.userContact ?: "No Mobile")
        holder.tvAddress.text = "Address: " + (order.userAddress ?: "No Address")
        holder.tvPrice.text = "Price: " + (order.price ?: "No Price")

        // Set the OnClickListener for the message button
        holder.btnMessage.setOnClickListener {
            val businessMobile = order.serviceProviderContact ?: "No Contact"
            sendMessageToWhatsApp(businessMobile, order)
        }
    }

    private fun sendMessageToWhatsApp(businessMobile: String, order: Order) {
        // Construct the message
        val message = "Hello, my order with UTR: ${order.utr} is not completed yet. " +
                "I am ${order.userName}, and my contact number is ${order.userContact}. " +
                "Could you please assist with the status?"

        // Create the WhatsApp URL
        val url = "https://wa.me/$businessMobile?text=${Uri.encode(message)}"

        // Open WhatsApp with the message
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle exception, e.g., if WhatsApp is not installed
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvBusinessTitle: TextView = itemView.findViewById(R.id.tvBusinessTitle)
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvBusinessDescription: TextView = itemView.findViewById(R.id.tvBusinessDescription)
        var tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        var tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var btnMessage: TextView= itemView.findViewById(R.id.btnMessage)  // Reference to the button
    }
}
