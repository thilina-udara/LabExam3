package com.example.labexam3

          import android.content.Intent
          import android.os.Bundle
          import android.view.View
          import android.widget.ArrayAdapter
          import android.widget.Button
          import android.widget.EditText
          import android.widget.RadioButton
          import android.widget.RadioGroup
          import android.widget.Spinner
          import android.widget.Toast
          import androidx.appcompat.app.AppCompatActivity
          import org.json.JSONArray
          import org.json.JSONObject
          import java.io.File
          import java.text.SimpleDateFormat
          import java.util.Date
          import java.util.Locale

          class AddTransactionActivity : AppCompatActivity() {
              private lateinit var amountEditText: EditText
              private lateinit var descriptionEditText: EditText
              private lateinit var typeRadioGroup: RadioGroup
              private lateinit var incomeRadioButton: RadioButton
              private lateinit var categorySpinner: Spinner
              private lateinit var saveButton: Button
              private lateinit var cancelButton: Button

              override fun onCreate(savedInstanceState: Bundle?) {
                  super.onCreate(savedInstanceState)
                  setContentView(R.layout.activity_add_transaction)

                  // Initialize UI components
                  amountEditText = findViewById(R.id.amountEditText)
                  descriptionEditText = findViewById(R.id.descriptionEditText)
                  typeRadioGroup = findViewById(R.id.typeRadioGroup)
                  incomeRadioButton = findViewById(R.id.incomeRadioButton)
                  categorySpinner = findViewById(R.id.categorySpinner)
                  saveButton = findViewById(R.id.saveButton)
                  cancelButton = findViewById(R.id.cancelButton)

                  // Set up category spinner
                  val categories = arrayOf(
                      "Food & Dining", "Shopping", "Housing", "Transportation",
                      "Entertainment", "Health", "Education", "Other"
                  )
                  val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                  categorySpinner.adapter = adapter

                  // Set default values
                  incomeRadioButton.isChecked = true

                  // Set up save button
                  saveButton.setOnClickListener {
                      saveTransaction()
                  }

                  // Set up cancel button
                  cancelButton.setOnClickListener {
                      finish()
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

              private fun saveTransaction() {
                  val amountStr = amountEditText.text.toString()
                  val description = descriptionEditText.text.toString()
                  val isIncome = incomeRadioButton.isChecked
                  val category = categorySpinner.selectedItem.toString()

                  if (amountStr.isEmpty() || description.isEmpty()) {
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

                      val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                      val currentDate = dateFormat.format(Date())

                      val transaction = JSONObject().apply {
                          put("amount", amount)
                          put("description", description)
                          put("type", if (isIncome) "INCOME" else "EXPENSE")
                          put("category", category)
                          put("date", currentDate)
                      }

                      transactions.put(transaction)
                      saveTransactions(transactions)

                      Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                      finish()

                  } catch (e: NumberFormatException) {
                      Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
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