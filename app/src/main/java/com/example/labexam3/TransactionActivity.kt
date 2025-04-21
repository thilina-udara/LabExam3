package com.example.labexam3

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.Button
    import android.widget.ListView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import org.json.JSONArray
    import org.json.JSONObject
    import java.io.File

    class TransactionActivity : AppCompatActivity() {

        private lateinit var transactionListView: ListView
        private lateinit var emptyTextView: TextView
        private lateinit var addTransactionButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_transaction)

            // Initialize UI components
            emptyTextView = findViewById(R.id.emptyTextView)
            transactionListView = findViewById(R.id.transactionListView)
            addTransactionButton = findViewById(R.id.addTransactionButton)

            // Load and display transactions
            loadTransactions()

            // Set up add transaction button
            addTransactionButton.setOnClickListener {
                startActivity(Intent(this, AddTransactionActivity::class.java))
            }

            // Set up navigation
            setupNavigation()
        }

        private fun setupNavigation() {
            findViewById<View>(R.id.navHome).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            // Transaction is current activity
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

        override fun onResume() {
            super.onResume()
            loadTransactions()
        }

        private fun loadTransactions() {
            val transactions = getAllTransactions()

            if (transactions.length() == 0) {
                emptyTextView.visibility = View.VISIBLE
                transactionListView.visibility = View.GONE
                return
            }

            emptyTextView.visibility = View.GONE
            transactionListView.visibility = View.VISIBLE

            val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
            val currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"

            // Create and set the adapter with edit and delete callbacks
            val adapter = TransactionAdapter(
                this,
                transactions,
                currencySymbol,
                onEditClick = { position -> editTransaction(position) },
                onDeleteClick = { position -> confirmDeleteTransaction(position) }
            )

            transactionListView.adapter = adapter
        }

        private fun editTransaction(position: Int) {
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("position", position)
            startActivity(intent)
        }

        private fun confirmDeleteTransaction(position: Int) {
            val transactions = getAllTransactions()
            if (position < transactions.length()) {
                val transaction = transactions.getJSONObject(position)
                val title = transaction.getString("title")

                AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete the transaction '$title'?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteTransaction(position)
                        loadTransactions()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun deleteTransaction(position: Int) {
            val transactions = getAllTransactions()
            if (transactions.length() > position) {
                val newTransactions = JSONArray()
                for (i in 0 until transactions.length()) {
                    if (i != position) {
                        newTransactions.put(transactions.get(i))
                    }
                }

                saveTransactions(newTransactions)
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
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

        private fun saveTransactions(transactions: JSONArray) {
            val file = File(filesDir, "transactions.json")
            file.writeText(transactions.toString())
        }
    }