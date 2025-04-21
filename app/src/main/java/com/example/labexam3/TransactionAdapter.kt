package com.example.labexam3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import org.json.JSONArray

class TransactionAdapter(
    private val context: Context,
    private val transactions: JSONArray,
    private val currencySymbol: String,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = transactions.length()

    override fun getItem(position: Int): Any = transactions.getJSONObject(position)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.transaction_list_item_with_buttons, null)

        val transaction = transactions.getJSONObject(position)
        val title = transaction.getString("title")
        val amount = transaction.getDouble("amount")
        val description = transaction.getString("description")
        val category = transaction.getString("category")
        val date = transaction.getString("date")
        val type = transaction.getString("type")

        // Set transaction title
        view.findViewById<TextView>(R.id.titleTextView).text = title

        // Set amount with appropriate color based on type
        val amountTV = view.findViewById<TextView>(R.id.amountTextView)
        val amountText = "$currencySymbol ${String.format("%,.2f", amount)}"
        amountTV.text = amountText
        amountTV.setTextColor(
            if (type == "INCOME")
                context.resources.getColor(android.R.color.holo_green_dark)
            else
                context.resources.getColor(android.R.color.holo_red_dark)
        )

        // Set description
        view.findViewById<TextView>(R.id.descriptionTextView).text = description

        // Set category
        view.findViewById<TextView>(R.id.categoryTextView).text = category

        // Set date
        view.findViewById<TextView>(R.id.dateTextView).text = date

        // Set button click listeners
        view.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            onEditClick(position)
        }

        view.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            onDeleteClick(position)
        }

        return view
    }
}