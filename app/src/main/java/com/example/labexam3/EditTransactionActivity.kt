package com.example.labexam3

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var incomeRadioButton: RadioButton
    private lateinit var expenseRadioButton: RadioButton
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var deleteButton: Button
    private lateinit var dateEditText: EditText
    private lateinit var datePickerButton: ImageButton

    private var selectedDate: String = ""
    private var transactionPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        supportActionBar?.title = "Edit Transaction"

        // Initialize UI components
        titleEditText = findViewById(R.id.titleEditText)
        amountEditText = findViewById(R.id.amountEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        incomeRadioButton = findViewById(R.id.incomeRadioButton)
        expenseRadioButton = findViewById(R.id.expenseRadioButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        deleteButton = findViewById(R.id.deleteButton)
        dateEditText = findViewById(R.id.dateEditText)
        datePickerButton = findViewById(R.id.datePickerButton)

        // Set up category spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Get transaction position from intent
        transactionPosition = intent.getIntExtra("position", -1)
        if (transactionPosition >= 0) {
            loadTransactionData()
        } else {
            // If no position provided, finish activity
            Toast.makeText(this, "Error loading transaction", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up date picker listeners
        dateEditText.setOnClickListener { showDatePicker() }
        datePickerButton.setOnClickListener { showDatePicker() }

        // Set up button listeners
        saveButton.setOnClickListener { updateTransaction() }
        cancelButton.setOnClickListener { finish() }
        deleteButton.setOnClickListener { deleteTransaction() }

        // Set up navigation
        setupNavigation()
    }

    private fun loadTransactionData() {
        val transactions = getAllTransactions()
        if (transactionPosition < transactions.length()) {
            val transaction = transactions.getJSONObject(transactionPosition)

            // Set title
            if (transaction.has("title")) {
                titleEditText.setText(transaction.getString("title"))
            }

            // Set amount
            amountEditText.setText(transaction.getDouble("amount").toString())

            // Set description
            descriptionEditText.setText(transaction.getString("description"))

            // Set transaction type
            val type = transaction.getString("type")
            if (type == "INCOME") {
                incomeRadioButton.isChecked = true
            } else {
                expenseRadioButton.isChecked = true
            }

            // Set category
            val category = transaction.getString("category")
            val categories = resources.getStringArray(R.array.categories)
            val position = categories.indexOf(category)
            if (position >= 0) {
                categorySpinner.setSelection(position)
            }

            // Set date
            selectedDate = transaction.getString("date")
            dateEditText.setText(selectedDate)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        if (selectedDate.isNotEmpty()) {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Use current date if there's an error parsing
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            dateEditText.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun updateTransaction() {
        val title = titleEditText.text.toString()
        val amountStr = amountEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val isIncome = incomeRadioButton.isChecked
        val category = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amount = amountStr.toDouble()
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                return
            }

            val transactions = getAllTransactions()

            if (transactionPosition >= 0 && transactionPosition < transactions.length()) {
                // Update existing transaction
                val updatedTransaction = JSONObject().apply {
                    put("title", title)
                    put("amount", amount)
                    put("description", description)
                    put("type", if (isIncome) "INCOME" else "EXPENSE")
                    put("category", category)
                    put("date", selectedDate)
                }

                // Replace at same position
                transactions.put(transactionPosition, updatedTransaction)
                saveTransactions(transactions)

                Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error updating transaction", Toast.LENGTH_SHORT).show()
            }

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTransaction() {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                val transactions = getAllTransactions()
                if (transactionPosition >= 0 && transactionPosition < transactions.length()) {
                    val updatedTransactions = JSONArray()

                    // Copy all transactions except the one to delete
                    for (i in 0 until transactions.length()) {
                        if (i != transactionPosition) {
                            updatedTransactions.put(transactions.get(i))
                        }
                    }

                    saveTransactions(updatedTransactions)
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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