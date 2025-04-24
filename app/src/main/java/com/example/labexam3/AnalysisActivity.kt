package com.example.labexam3

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalysisActivity : AppCompatActivity() {
    private lateinit var totalSpendingTextView: TextView
    private lateinit var budgetStatusTextView: TextView
    private lateinit var progressSpending: ProgressBar
    private lateinit var insightsTextView: TextView
    private lateinit var analysisListView: ListView
    private lateinit var noDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // Initialize UI components
        totalSpendingTextView = findViewById(R.id.totalSpendingTextView)
        budgetStatusTextView = findViewById(R.id.budgetStatusTextView)
        progressSpending = findViewById(R.id.progressSpending)
        insightsTextView = findViewById(R.id.insightsTextView)
        analysisListView = findViewById(R.id.analysisListView)
        noDataTextView = findViewById(R.id.noDataTextView)

        // Load and display spending analysis
        loadAnalysis()

        // Set up navigation
        setupNavigation()

        // Highlight the Analysis navigation item
        BottomNavHelper.updateSelectedNav(this, R.id.navAnalysis)
    }

    override fun onResume() {
        super.onResume()
        loadAnalysis()

        // Re-highlight the Analysis navigation item when returning to this activity
        BottomNavHelper.updateSelectedNav(this, R.id.navAnalysis)
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
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"
        val monthlyBudget = sharedPreferences.getFloat("monthly_budget", 10000f).toDouble()

        if (transactions.length() == 0) {
            noDataTextView.visibility = View.VISIBLE
            analysisListView.visibility = View.GONE
            totalSpendingTextView.text = "Total Spending: ${currencySymbol}0.00"
            budgetStatusTextView.text = "No expenses recorded yet"
            progressSpending.progress = 0
            insightsTextView.text = "Add some transactions to see insights"
            return
        }

        // Calculate category-wise spending
        val categorySpending = mutableMapOf<String, Double>()
        var totalSpending = 0.0

        // Track date ranges for analysis
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // For tracking previous month data
        val dailySpending = mutableMapOf<String, Double>()
        val previousMonthSpending = mutableMapOf<String, Double>()
        var previousMonthTotal = 0.0

        for (i in 0 until transactions.length()) {
            val transaction = transactions.getJSONObject(i)
            if (transaction.getString("type") == "EXPENSE") {
                val category = transaction.getString("category")
                val amount = transaction.getDouble("amount")
                val dateStr = transaction.getString("date")

                try {
                    val date = dateFormat.parse(dateStr)
                    val cal = Calendar.getInstance()
                    if (date != null) {
                        cal.time = date
                    }

                    val transMonth = cal.get(Calendar.MONTH)
                    val transYear = cal.get(Calendar.YEAR)

                    // Current month transactions
                    if (transMonth == currentMonth && transYear == currentYear) {
                        categorySpending[category] = (categorySpending[category] ?: 0.0) + amount
                        totalSpending += amount

                        // Track daily spending
                        dailySpending[dateStr] = (dailySpending[dateStr] ?: 0.0) + amount
                    }
                    // Previous month transactions
                    else if (transMonth == (currentMonth - 1 + 12) % 12 &&
                            (if (transMonth == 11 && currentMonth == 0) transYear == currentYear - 1 else transYear == currentYear)) {
                        previousMonthSpending[category] = (previousMonthSpending[category] ?: 0.0) + amount
                        previousMonthTotal += amount
                    }
                } catch (e: Exception) {
                    // Skip invalid dates
                }
            }
        }

        // Update total spending display
        totalSpendingTextView.text = "Total Spending: ${currencySymbol}${"%.2f".format(totalSpending)}"

        // Update budget progress
        updateBudgetProgress(totalSpending, monthlyBudget)

        if (totalSpending > 0) {
            noDataTextView.visibility = View.GONE
            analysisListView.visibility = View.VISIBLE

            // Generate insights
            val insights = generateInsights(categorySpending, totalSpending, previousMonthSpending, previousMonthTotal)
            insightsTextView.text = insights.joinToString("\n\n")

            // Prepare data for ListView with custom adapter
            val categoryDataList = categorySpending.entries
                .sortedByDescending { it.value }
                .map { entry ->
                    CategoryAdapter.CategoryData(
                        category = entry.key,
                        amount = entry.value,
                        percentage = (entry.value / totalSpending) * 100,
                        formattedAmount = "${currencySymbol}${"%.2f".format(entry.value)}"
                    )
                }

            val adapter = CategoryAdapter(this, categoryDataList)
            analysisListView.adapter = adapter
        } else {
            noDataTextView.visibility = View.VISIBLE
            analysisListView.visibility = View.GONE
        }
    }

    private fun updateBudgetProgress(totalSpent: Double, budget: Double) {
        val percentage = (totalSpent / budget * 100).toInt()
        progressSpending.progress = percentage

        // Set color based on percentage
        val color = when {
            percentage > 90 -> Color.parseColor("#f44336") // Red
            percentage > 75 -> Color.parseColor("#FF9800") // Orange
            percentage > 50 -> Color.parseColor("#FFEB3B") // Yellow
            else -> Color.parseColor("#4CAF50") // Green
        }

        progressSpending.progressTintList = ColorStateList.valueOf(color)

        // Update budget status text
        val remainingBudget = budget - totalSpent
        val status = when {
            remainingBudget < 0 -> "You've exceeded your budget by ${"%,.2f".format(Math.abs(remainingBudget))}"
            percentage > 90 -> "Warning: You've used ${percentage}% of your budget"
            percentage > 75 -> "Caution: You've used ${percentage}% of your budget"
            else -> "You've used ${percentage}% of your budget"
        }

        budgetStatusTextView.text = status
    }

    private fun generateInsights(
        categorySpending: Map<String, Double>,
        totalSpending: Double,
        previousMonthSpending: Map<String, Double>,
        previousMonthTotal: Double
    ): List<String> {
        val insights = mutableListOf<String>()

        // Find highest spending category
        val highestCategory = categorySpending.entries.maxByOrNull { it.value }
        highestCategory?.let {
            val percentage = (it.value / totalSpending * 100).toInt()
            insights.add("You spent the most on ${it.key} (${percentage}% of total)")
        }

        // Compare with previous month
        if (previousMonthTotal > 0) {
            val change = ((totalSpending - previousMonthTotal) / previousMonthTotal * 100).toInt()
            if (change > 0) {
                insights.add("Your spending increased by ${change}% compared to last month")
            } else if (change < 0) {
                insights.add("Your spending decreased by ${Math.abs(change)}% compared to last month")
            }

            // Look for categories with significant changes
            for (category in categorySpending.keys) {
                val current = categorySpending[category] ?: 0.0
                val previous = previousMonthSpending[category] ?: 0.0

                if (previous > 0 && current > 0) {
                    val categoryChange = ((current - previous) / previous * 100).toInt()
                    if (categoryChange > 30 && current > totalSpending * 0.1) {
                        insights.add("${category} spending increased by ${categoryChange}% from last month")
                    }
                }
            }
        }

        // Budget-based insight
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val budget = sharedPreferences.getFloat("monthly_budget", 10000f).toDouble()
        val remainingBudget = budget - totalSpending
        val remainingDays = getRemainingDaysInMonth()

        if (remainingDays > 0 && remainingBudget > 0) {
            val dailyBudget = remainingBudget / remainingDays
            insights.add("Budget tip: You have ${"%,.2f".format(remainingBudget)} left to spend (${"%,.2f".format(dailyBudget)} per day)")
        }

        return insights
    }

    private fun getRemainingDaysInMonth(): Int {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return lastDay - today + 1
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