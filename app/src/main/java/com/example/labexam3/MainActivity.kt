package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var balanceTextView: TextView
    private lateinit var incomeTextView: TextView
    private lateinit var expensesTextView: TextView
    private lateinit var addIncomeButton: Button
    private lateinit var addExpenseButton: Button
    private lateinit var recentTransactionsListView: ListView
    private lateinit var noTransactionsTextView: TextView
    private lateinit var topBudgetContainer: LinearLayout
    private lateinit var noBudgetTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        balanceTextView = findViewById(R.id.balanceTextView)
        incomeTextView = findViewById(R.id.incomeTextView)
        expensesTextView = findViewById(R.id.expensesTextView)
        addIncomeButton = findViewById(R.id.addIncomeButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        recentTransactionsListView = findViewById(R.id.recentTransactionsListView)
        noTransactionsTextView = findViewById(R.id.noTransactionsTextView)
        topBudgetContainer = findViewById(R.id.topBudgetContainer)
        noBudgetTextView = findViewById(R.id.noBudgetTextView)

        // Set up quick action buttons
        addIncomeButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("defaultType", "INCOME")
            startActivity(intent)
        }

        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("defaultType", "EXPENSE")
            startActivity(intent)
        }

        // Set up view all buttons
        findViewById<Button>(R.id.viewAllTransactionsButton).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        findViewById<Button>(R.id.viewAllBudgetsButton).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // Set up navigation
        setupNavigation()

        // Highlight the Home navigation item
        BottomNavHelper.updateSelectedNav(this, R.id.navHome)

        // Load dashboard data
        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard() // Refresh data when returning to activity
        // Re-highlight Home navigation item when returning to this activity
        BottomNavHelper.updateSelectedNav(this, R.id.navHome)
    }

    private fun loadDashboard() {
        loadFinancialSummary()
        loadRecentTransactions()
        loadBudgetStatus()
    }

    private fun loadFinancialSummary() {
        val transactions = getAllTransactions()
        var totalIncome = 0.0
        var totalExpenses = 0.0

        for (i in 0 until transactions.length()) {
            val transaction = transactions.getJSONObject(i)
            val amount = transaction.getDouble("amount")

            if (transaction.getString("type") == "INCOME") {
                totalIncome += amount
            } else {
                totalExpenses += amount
            }
        }

        val balance = totalIncome - totalExpenses

        // Get currency preference
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"

        // Update UI
        balanceTextView.text = "$currencySymbol${"%.2f".format(balance)}"
        incomeTextView.text = "$currencySymbol${"%.2f".format(totalIncome)}"
        expensesTextView.text = "$currencySymbol${"%.2f".format(totalExpenses)}"
    }

    private fun loadRecentTransactions() {
        val transactions = getAllTransactions()
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"

        if (transactions.length() == 0) {
            noTransactionsTextView.visibility = View.VISIBLE
            recentTransactionsListView.visibility = View.GONE
            return
        }

        noTransactionsTextView.visibility = View.GONE
        recentTransactionsListView.visibility = View.VISIBLE

        val recentList = mutableListOf<Map<String, String>>()
        val limit = minOf(5, transactions.length())

        // Get most recent transactions
        for (i in transactions.length() - 1 downTo maxOf(0, transactions.length() - limit)) {
            val transaction = transactions.getJSONObject(i)
            val amount = transaction.getDouble("amount")
            val type = transaction.getString("type")
            val sign = if (type == "INCOME") "+" else "-"
            val amountColor = if (type == "INCOME") "#4caf50" else "#f44336"

            recentList.add(mapOf(
                "description" to transaction.getString("description"),
                "amount" to "$sign$currencySymbol${"%.2f".format(amount)}",
                "category" to transaction.getString("category"),
                "date" to transaction.getString("date"),
                "amountColor" to amountColor
            ))
        }

        val adapter = SimpleAdapter(
            this,
            recentList,
            R.layout.transaction_list_item,
            arrayOf("description", "amount", "category", "date"),
            intArrayOf(R.id.descriptionTextView, R.id.amountTextView, R.id.categoryTextView, R.id.dateTextView)
        )

        recentTransactionsListView.adapter = adapter
    }

    private fun loadBudgetStatus() {
        val budgets = getAllBudgets()

        if (budgets.length() == 0) {
            noBudgetTextView.visibility = View.VISIBLE
            topBudgetContainer.visibility = View.GONE
            return
        }

        noBudgetTextView.visibility = View.GONE
        topBudgetContainer.visibility = View.VISIBLE
        topBudgetContainer.removeAllViews()

        // Get currency preference
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"

        // Calculate spending by category
        val categorySpending = calculateCategorySpending()

        // Create budget items
        val limit = minOf(3, budgets.length()) // Show top 3 budgets
        for (i in 0 until limit) {
            val budget = budgets.getJSONObject(i)
            val category = budget.getString("category")
            val budgetAmount = budget.getDouble("amount")
            val spent = categorySpending[category] ?: 0.0

            val percentage = (spent / budgetAmount * 100).toInt()
            val remaining = budgetAmount - spent

            val budgetView = LayoutInflater.from(this).inflate(
                R.layout.home_budget_item, topBudgetContainer, false
            )

            val categoryTextView = budgetView.findViewById<TextView>(R.id.categoryName)
            val progressTextView = budgetView.findViewById<TextView>(R.id.progressText)
            val remainingTextView = budgetView.findViewById<TextView>(R.id.remainingAmount)

            categoryTextView.text = category
            progressTextView.text = "$percentage%"
            remainingTextView.text = "Remaining: $currencySymbol${"%.2f".format(remaining)}"

            // Change color based on budget status
            when {
                percentage >= 100 -> progressTextView.setTextColor(getColor(android.R.color.holo_red_dark))
                percentage >= 80 -> progressTextView.setTextColor(getColor(android.R.color.holo_orange_dark))
                else -> progressTextView.setTextColor(getColor(android.R.color.holo_green_dark))
            }

            topBudgetContainer.addView(budgetView)
        }
    }

    private fun calculateCategorySpending(): Map<String, Double> {
        val transactions = getAllTransactions()
        val categorySpending = mutableMapOf<String, Double>()

        for (i in 0 until transactions.length()) {
            val transaction = transactions.getJSONObject(i)
            if (transaction.getString("type") == "EXPENSE") {
                val category = transaction.getString("category")
                val amount = transaction.getDouble("amount")
                categorySpending[category] = (categorySpending[category] ?: 0.0) + amount
            }
        }

        return categorySpending
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

    private fun getAllBudgets(): JSONArray {
        val file = File(filesDir, "budgets.json")
        if (!file.exists() || file.readText().isEmpty()) {
            return JSONArray()
        }
        return try {
            JSONArray(file.readText())
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun setupNavigation() {
        // Home is current activity
        findViewById<View>(R.id.navHome).setOnClickListener {
            // Already in this activity
        }

        findViewById<View>(R.id.navTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        findViewById<View>(R.id.navBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        findViewById<View>(R.id.navAnalysis).setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}