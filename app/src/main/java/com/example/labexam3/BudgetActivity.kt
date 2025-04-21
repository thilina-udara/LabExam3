package com.example.labexam3

      import android.content.Intent
      import android.os.Bundle
      import android.view.View
      import android.widget.ArrayAdapter
      import android.widget.Button
      import android.widget.EditText
      import android.widget.ListView
      import android.widget.SimpleAdapter
      import android.widget.Spinner
      import android.widget.TextView
      import android.widget.Toast
      import androidx.appcompat.app.AlertDialog
      import androidx.appcompat.app.AppCompatActivity
      import org.json.JSONArray
      import org.json.JSONObject
      import java.io.File

      class BudgetActivity : AppCompatActivity() {

          private lateinit var budgetListView: ListView
          private lateinit var emptyTextView: TextView
          private lateinit var addBudgetButton: Button

          override fun onCreate(savedInstanceState: Bundle?) {
              super.onCreate(savedInstanceState)
              setContentView(R.layout.activity_budget)

              // Initialize UI components
              emptyTextView = findViewById<TextView>(R.id.emptyTextView)
              budgetListView = findViewById<ListView>(R.id.budgetListView)
              addBudgetButton = findViewById<Button>(R.id.addBudgetButton)

              // Load and display budgets
              loadBudgets()

              // Set up add budget button
              addBudgetButton.setOnClickListener {
                  showAddBudgetDialog()
              }

              // Set long click listener for deletion
              budgetListView.setOnItemLongClickListener { _, _, position, _ ->
                  val budgets = getAllBudgets()
                  if (budgets.length() > position) {
                      deleteBudget(position)
                      loadBudgets()
                      return@setOnItemLongClickListener true
                  }
                  false
              }

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

              // Budget is current activity
              findViewById<View>(R.id.navBudget).setOnClickListener {
                  // Already in this activity
              }

              findViewById<View>(R.id.navAnalysis).setOnClickListener {
                  startActivity(Intent(this, AnalysisActivity::class.java))
                  finish()
              }

              findViewById<View>(R.id.navSettings).setOnClickListener {
                  startActivity(Intent(this, SettingsActivity::class.java))
                  finish()
              }
          }

          override fun onResume() {
              super.onResume()
              loadBudgets()
          }

          private fun loadBudgets() {
              val budgets = getAllBudgets()

              if (budgets.length() == 0) {
                  emptyTextView.visibility = View.VISIBLE
                  budgetListView.visibility = View.GONE
                  return
              } else {
                  emptyTextView.visibility = View.GONE
                  budgetListView.visibility = View.VISIBLE
              }

              val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
              val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"
              val budgetList = mutableListOf<Map<String, String>>()

              // Get all transactions for spending calculations
              val transactions = getAllTransactions()
              val categorySpending = mutableMapOf<String, Double>()

              // Calculate spending per category
              for (i in 0 until transactions.length()) {
                  val transaction = transactions.getJSONObject(i)
                  if (transaction.getString("type") == "EXPENSE") {
                      val category = transaction.getString("category")
                      val amount = transaction.getDouble("amount")
                      categorySpending[category] = (categorySpending[category] ?: 0.0) + amount
                  }
              }

              // Create budget list items
              for (i in 0 until budgets.length()) {
                  val budget = budgets.getJSONObject(i)
                  val category = budget.getString("category")
                  val budgetAmount = budget.getDouble("amount")
                  val spent = categorySpending[category] ?: 0.0
                  val remaining = budgetAmount - spent
                  val percentage = if (budgetAmount > 0) (spent / budgetAmount) * 100 else 0.0

                  val status = when {
                      percentage >= 100 -> "Over Budget"
                      percentage >= 80 -> "Warning"
                      else -> "On Track"
                  }

                  budgetList.add(mapOf(
                      "category" to category,
                      "budget" to "$currencySymbol${"%.2f".format(budgetAmount)}",
                      "spent" to "$currencySymbol${"%.2f".format(spent)}",
                      "remaining" to "$currencySymbol${"%.2f".format(remaining)}",
                      "status" to status
                  ))
              }

              val adapter = SimpleAdapter(
                  this,
                  budgetList,
                  android.R.layout.simple_list_item_2,
                  arrayOf("category", "budget"),
                  intArrayOf(android.R.id.text1, android.R.id.text2)
              )

              budgetListView.adapter = adapter
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

          private fun showAddBudgetDialog() {
              val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
              val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
              val budgetAmountEditText = dialogView.findViewById<EditText>(R.id.budgetAmountEditText)

              // Set up category spinner
              val categories = arrayOf(
                  "Food & Dining", "Shopping", "Housing", "Transportation",
                  "Entertainment", "Health", "Education", "Other"
              )
              val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
              adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
              categorySpinner.adapter = adapter

              AlertDialog.Builder(this)
                  .setTitle("Add New Budget")
                  .setView(dialogView)
                  .setPositiveButton("Save") { _, _ ->
                      val category = categorySpinner.selectedItem.toString()
                      val amountStr = budgetAmountEditText.text.toString()

                      if (amountStr.isEmpty()) {
                          Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                          return@setPositiveButton
                      }

                      try {
                          val amount = amountStr.toDouble()
                          if (amount <= 0) {
                              Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                              return@setPositiveButton
                          }

                          saveBudget(category, amount)
                          loadBudgets()
                      } catch (e: NumberFormatException) {
                          Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
                      }
                  }
                  .setNegativeButton("Cancel", null)
                  .show()
          }

          private fun saveBudget(category: String, amount: Double) {
              val budgets = getAllBudgets()

              // Check if budget for this category already exists
              for (i in 0 until budgets.length()) {
                  val budget = budgets.getJSONObject(i)
                  if (budget.getString("category") == category) {
                      // Update existing budget
                      budget.put("amount", amount)
                      saveBudgets(budgets)
                      Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show()
                      return
                  }
              }

              // Add new budget
              val budget = JSONObject().apply {
                  put("category", category)
                  put("amount", amount)
              }

              budgets.put(budget)
              saveBudgets(budgets)
              Toast.makeText(this, "Budget added", Toast.LENGTH_SHORT).show()
          }

          private fun saveBudgets(budgets: JSONArray) {
              val file = File(filesDir, "budgets.json")
              file.writeText(budgets.toString())
          }

          private fun deleteBudget(position: Int) {
              val budgets = getAllBudgets()
              if (budgets.length() > position) {
                  val newBudgets = JSONArray()
                  for (i in 0 until budgets.length()) {
                      if (i != position) {
                          newBudgets.put(budgets.get(i))
                      }
                  }

                  saveBudgets(newBudgets)
                  Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show()
              }
          }
      }