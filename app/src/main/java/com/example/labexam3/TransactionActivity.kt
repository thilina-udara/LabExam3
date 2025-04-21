package com.example.labexam3

        import android.content.Intent
        import android.os.Bundle
        import android.view.View
        import android.widget.AdapterView
        import android.widget.ListView
        import android.widget.SimpleAdapter
        import android.widget.TextView
        import android.widget.Toast
        import androidx.appcompat.app.AppCompatActivity
        import org.json.JSONArray
        import java.io.File
        import java.text.SimpleDateFormat
        import java.util.Date
        import java.util.Locale

        class TransactionActivity : AppCompatActivity() {

            private lateinit var transactionListView: ListView
            private lateinit var emptyTextView: TextView

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_transaction)

                // Initialize UI components
                transactionListView = findViewById(R.id.transactionListView)
                emptyTextView = findViewById(R.id.emptyTextView)

                // Load and display transactions
                loadTransactions()

                // Set click listener for transaction items
                transactionListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    val transactions = getAllTransactions()
                    if (transactions.length() > 0 && position < transactions.length()) {
                        val transaction = transactions.getJSONObject(position)
                        Toast.makeText(this, "Transaction: ${transaction.getString("description")}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Set long click listener for deletion
                transactionListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                    deleteTransaction(position)
                    loadTransactions()
                    true
                }

                // Set up navigation
                setupNavigation()
            }

            override fun onResume() {
                super.onResume()
                loadTransactions() // Refresh on resume
            }

            private fun loadTransactions() {
                val transactions = getAllTransactions()

                if (transactions.length() == 0) {
                    emptyTextView.visibility = View.VISIBLE
                    transactionListView.visibility = View.GONE
                    return
                } else {
                    emptyTextView.visibility = View.GONE
                    transactionListView.visibility = View.VISIBLE
                }

                val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
                val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"
                val transactionList = mutableListOf<Map<String, String>>()

                for (i in 0 until transactions.length()) {
                    val transaction = transactions.getJSONObject(i)

                    val amount = transaction.getDouble("amount")
                    val type = transaction.getString("type")
                    val formattedAmount = if (type == "INCOME")
                        "+$currencySymbol${"%.2f".format(amount)}"
                    else
                        "-$currencySymbol${"%.2f".format(amount)}"

                    transactionList.add(mapOf(
                        "description" to transaction.getString("description"),
                        "amount" to formattedAmount,
                        "category" to transaction.getString("category"),
                        "date" to transaction.getString("date")
                    ))
                }

                val adapter = SimpleAdapter(
                    this,
                    transactionList,
                    R.layout.transaction_list_item,
                    arrayOf("description", "amount", "category", "date"),
                    intArrayOf(R.id.descriptionTextView, R.id.amountTextView, R.id.categoryTextView, R.id.dateTextView)
                )

                transactionListView.adapter = adapter
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

            private fun deleteTransaction(position: Int) {
                val jsonArray = getAllTransactions()
                if (jsonArray.length() > position) {
                    val newJsonArray = JSONArray()
                    for (i in 0 until jsonArray.length()) {
                        if (i != position) {
                            newJsonArray.put(jsonArray.get(i))
                        }
                    }

                    val file = File(filesDir, "transactions.json")
                    file.writeText(newJsonArray.toString())
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                }
            }

            private fun setupNavigation() {
                findViewById<View>(R.id.navHome).setOnClickListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                // Transactions is current activity
                findViewById<View>(R.id.navTransactions).setOnClickListener {
                    // Already in this activity
                }

                findViewById<View>(R.id.navBudget).setOnClickListener {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    finish()
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
        }