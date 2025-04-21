package com.example.labexam3

        import android.content.Context
        import android.graphics.Color
        import android.graphics.PorterDuff
        import android.view.LayoutInflater
        import android.view.View
        import android.view.ViewGroup
        import android.widget.BaseAdapter
        import android.widget.ImageView
        import android.widget.ProgressBar
        import android.widget.TextView

        class CategoryAdapter(
            private val context: Context,
            private val categories: List<CategoryData>
        ) : BaseAdapter() {

            data class CategoryData(
                val category: String,
                val amount: Double,
                val percentage: Double,
                val formattedAmount: String
            )

            override fun getCount(): Int = categories.size
            override fun getItem(position: Int) = categories[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(
                    R.layout.category_list_item, parent, false
                )

                val category = categories[position]
                val categoryText = view.findViewById<TextView>(R.id.tvCategoryName)
                val amountText = view.findViewById<TextView>(R.id.tvBudgetAmount)
                val percentageText = view.findViewById<TextView>(R.id.tvSpentAmount)
                val progressBar = view.findViewById<ProgressBar>(R.id.progressCategory)

                // You need to add this ImageView to your layout
                val iconView = view.findViewById<ImageView>(R.id.categoryIcon)

                categoryText.text = category.category
                amountText.text = category.formattedAmount
                percentageText.text = "${"%.1f".format(category.percentage)}%"
                progressBar.progress = category.percentage.toInt()

                // Only set icon if the view exists
                iconView?.setImageResource(getCategoryIcon(category.category))

                // Set progress bar color based on category
                val progressDrawable = progressBar.progressDrawable.mutate()
                progressDrawable.setColorFilter(getCategoryColor(category.category), PorterDuff.Mode.SRC_IN)
                progressBar.progressDrawable = progressDrawable

                return view
            }

            private fun getCategoryIcon(category: String): Int {
                return when (category) {
                    "Food & Dining" -> R.drawable.diet
                    "Shopping" -> R.drawable.shoppingcart
                    "Housing" -> R.drawable.house
                    "Transportation" -> R.drawable.transaction
                    "Entertainment" -> R.drawable.dancer
                    "Health" -> R.drawable.helath
                    "Education" -> R.drawable.homework
                    else -> R.drawable.other
                }
            }

            private fun getCategoryColor(category: String): Int {
                return when (category) {
                    "Food & Dining" -> Color.parseColor("#FF9800") // Orange
                    "Shopping" -> Color.parseColor("#E91E63") // Pink
                    "Housing" -> Color.parseColor("#673AB7") // Deep Purple
                    "Transportation" -> Color.parseColor("#03A9F4") // Light Blue
                    "Entertainment" -> Color.parseColor("#4CAF50") // Green
                    "Health" -> Color.parseColor("#F44336") // Red
                    "Education" -> Color.parseColor("#2196F3") // Blue
                    else -> Color.parseColor("#607D8B") // Blue Grey
                }
            }
        }