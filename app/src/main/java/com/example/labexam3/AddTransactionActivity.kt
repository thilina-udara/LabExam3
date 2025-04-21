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
          import androidx.appcompat.app.AppCompatActivity
          import org.json.JSONArray
          import org.json.JSONObject
          import java.io.File
          import java.text.SimpleDateFormat
          import java.util.Calendar
          import java.util.Date
          import java.util.Locale

          class AddTransactionActivity : AppCompatActivity() {
              private lateinit var titleEditText: EditText
              private lateinit var amountEditText: EditText
              private lateinit var descriptionEditText: EditText
              private lateinit var typeRadioGroup: RadioGroup
              private lateinit var incomeRadioButton: RadioButton
              private lateinit var categorySpinner: Spinner
              private lateinit var saveButton: Button
              private lateinit var cancelButton: Button
              // Date fields
              private lateinit var dateEditText: EditText
              private lateinit var datePickerButton: ImageButton
              private var selectedDate: String = ""

              override fun onCreate(savedInstanceState: Bundle?) {
                  super.onCreate(savedInstanceState)
                  setContentView(R.layout.activity_add_transaction)

                  supportActionBar?.title = "Add New Transaction"

                  // Initialize UI components
                  titleEditText = findViewById(R.id.titleEditText)
                  amountEditText = findViewById(R.id.amountEditText)
                  descriptionEditText = findViewById(R.id.descriptionEditText)
                  typeRadioGroup = findViewById(R.id.typeRadioGroup)
                  incomeRadioButton = findViewById(R.id.incomeRadioButton)
                  categorySpinner = findViewById(R.id.categorySpinner)
                  saveButton = findViewById(R.id.saveButton)
                  cancelButton = findViewById(R.id.cancelButton)

                  // Initialize date components
                  dateEditText = findViewById(R.id.dateEditText)
                  datePickerButton = findViewById(R.id.datePickerButton)

                  // Set current date as default
                  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                  selectedDate = dateFormat.format(Date())
                  dateEditText.setText(selectedDate)

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

                  // Set up date picker listeners
                  dateEditText.setOnClickListener { showDatePicker() }
                  datePickerButton.setOnClickListener { showDatePicker() }

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

              private fun saveTransaction() {
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

                      val transaction = JSONObject().apply {
                          put("title", title)
                          put("amount", amount)
                          put("description", description)
                          put("type", if (isIncome) "INCOME" else "EXPENSE")
                          put("category", category)
                          put("date", selectedDate)
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