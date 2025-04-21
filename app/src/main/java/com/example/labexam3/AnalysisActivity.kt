package com.example.labexam3

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.ListView
    import android.widget.SimpleAdapter
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import org.json.JSONArray
    import java.io.File

    class AnalysisActivity : AppCompatActivity() {
        private lateinit var totalSpendingTextView: TextView
        private lateinit var analysisListView: ListView
        private lateinit var noDataTextView: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_analysis)

            // Initialize UI components
            totalSpendingTextView = findViewById(R.id.totalSpendingTextView)
            analysisListView = findViewById(R.id.analysisListView)
            noDataTextView = findViewById(R.id.noDataTextView)

            // Load and display spending analysis
            loadAnalysis()

            // Set up navigation
            setupNavigation()
        }

        private fun setupNavigation() {
            findViewById<View>(R.id.navHome).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            findViewById<View>(R.id.navTransactions).setOnClickListener {
                startActivity(Intent(this, TransactionActivity::class.java))
                finish()
            }

            findViewById<View>(R.id.navBudget).setOnClickListener {
                startActivity(Intent(this, BudgetActivity::class.java))
                finish()
            }

            // Analysis is current activity
            findViewById<View>(R.id.navAnalysis).setOnClickListener {
                // Already in this activity
            }

            findViewById<View>(R.id.navSettings).setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }
        }

        private fun loadAnalysis() {
            val transactions = getAllTransactions()

            if (transactions.length() == 0) {
                noDataTextView.visibility = View.VISIBLE
                analysisListView.visibility = View.GONE
                totalSpendingTextView.text = "Total Spending: $0.00"
                return
            }

            // Calculate category-wise spending
            val categorySpending = mutableMapOf<String, Double>()
            var totalSpending = 0.0

            for (i in 0 until transactions.length()) {
                val transaction = transactions.getJSONObject(i)
                if (transaction.getString("type") == "EXPENSE") {
                    val category = transaction.getString("category")
                    val amount = transaction.getDouble("amount")

                    categorySpending[category] = (categorySpending[category] ?: 0.0) + amount
                    totalSpending += amount
                }
            }

            // Update UI
            val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
            val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"
            totalSpendingTextView.text = "Total Spending: $currencySymbol${"%.2f".format(totalSpending)}"

            if (totalSpending > 0) {
                noDataTextView.visibility = View.GONE
                analysisListView.visibility = View.VISIBLE

                // Prepare data for ListView
                val analysisList = mutableListOf<Map<String, String>>()

                // Sort categories by spending amount (descending)
                val sortedCategories = categorySpending.entries.sortedByDescending { it.value }

                for ((category, amount) in sortedCategories) {
                    val percentage = (amount / totalSpending) * 100

                    analysisList.add(mapOf(
                        "category" to category,
                        "amount" to "$currencySymbol${"%.2f".format(amount)}",
                        "percentage" to "${"%.1f".format(percentage)}%"
                    ))
                }

                val adapter = SimpleAdapter(
                    this,
                    analysisList,
                    R.layout.analysis_list_item,
                    arrayOf("category", "amount", "percentage"),
                    intArrayOf(R.id.categoryTextView, R.id.amountTextView, R.id.percentageTextView)
                )

                analysisListView.adapter = adapter
            } else {
                noDataTextView.visibility = View.VISIBLE
                analysisListView.visibility = View.GONE
            }
        }

        private fun getAllTransactions(): JSONArray {
            val file = File(filesDir, "transactions.json")
            if (!file.exists() || file.readText().isEmpty()) {
                return JSONArray()
            }

            return try {
                JSONArray(file.readText())
            } catch (e: Exception) {
                JSONArray()
            }
        }
    }