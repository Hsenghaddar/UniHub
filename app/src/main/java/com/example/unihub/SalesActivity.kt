package com.example.unihub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ActivitySalesBinding
import com.example.unihub.databinding.ItemSaleBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

/**
 * Activity for viewing a summary of the user's marketplace sales.
 *
 * This activity displays total revenue and the number of items sold, along with
 * a detailed list of individual transactions. It queries the `UserDatabaseHelper`
 * for sales records associated with the current user's UID.
 */
class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar with back navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        db = UserDatabaseHelper(this)
        loadSalesData()
    }

    /**
     * Fetches sales data from the database, calculates aggregates, and updates the UI.
     */
    private fun loadSalesData() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sales = db.getSalesForUser(currentUserUid)

        // Calculate aggregate statistics for the dashboard
        val totalRevenue = sales.sumOf { it.totalPrice }
        val itemsSold = sales.sumOf { it.quantity }

        binding.tvTotalRevenue.text = String.format(Locale.getDefault(), "$%.2f", totalRevenue)
        binding.tvItemsSold.text = itemsSold.toString()

        // Setup RecyclerView for individual sale entries
        binding.rvSales.layoutManager = LinearLayoutManager(this)
        binding.rvSales.adapter = SalesAdapter(sales)
    }

    /**
     * Adapter class for displaying individual sale records.
     */
    class SalesAdapter(private val sales: List<Sale>) : RecyclerView.Adapter<SalesAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSaleBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sale = sales[position]
            holder.binding.apply {
                tvSaleItemTitle.text = sale.itemTitle
                tvSaleBuyerName.text = "Buyer: ${sale.buyerName}"
                tvSaleQuantity.text = "Qty: ${sale.quantity}"
                tvSalePrice.text = String.format(Locale.getDefault(), "$%.2f", sale.totalPrice)
                tvSaleDate.text = sale.timestamp
            }
        }

        override fun getItemCount() = sales.size
    }
}
