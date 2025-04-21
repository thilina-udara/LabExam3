package com.example.labexam3

    import android.content.Context
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.BaseAdapter
    import android.widget.ImageButton
    import android.widget.TextView
    import org.json.JSONArray

    class BudgetAdapter(
        private val context: Context,
        private val budgets: JSONArray,
        private val categorySpending: Map<String, Double>,
        private val currencySymbol: String,
        private val onEditClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : BaseAdapter() {

        override fun getCount(): Int = budgets.length()

        override fun getItem(position: Int): Any = budgets.getJSONObject(position)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.budget_list_item, parent, false)

            val budget = budgets.getJSONObject(position)
            val category = budget.getString("category")
            val budgetAmount = budget.getDouble("amount")
            val spent = categorySpending[category] ?: 0.0
            val remaining = budgetAmount - spent

            // Set category name
            view.findViewById<TextView>(R.id.categoryTextView).text = category

            // Set budget amount
            view.findViewById<TextView>(R.id.budgetAmountTextView).text =
                "$currencySymbol ${String.format("%,.2f", budgetAmount)}"

            // Set spent amount
            view.findViewById<TextView>(R.id.spentTextView).text =
                "$currencySymbol ${String.format("%,.2f", spent)}"

            // Set remaining amount
            val remainingTV = view.findViewById<TextView>(R.id.remainingTextView)
            remainingTV.text = "$currencySymbol ${String.format("%,.2f", remaining)}"

            // Set color based on remaining amount
            if (remaining < 0) {
                remainingTV.setTextColor(context.getColor(android.R.color.holo_red_dark))
            } else {
                remainingTV.setTextColor(context.getColor(android.R.color.holo_green_dark))
            }

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