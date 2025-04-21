package com.example.labexam3

        import android.content.Intent
        import android.os.Bundle
        import android.view.View
        import android.widget.ArrayAdapter
        import android.widget.Button
        import android.widget.Spinner
        import android.widget.Toast
        import androidx.appcompat.app.AppCompatActivity

        class SettingsActivity : AppCompatActivity() {
            private lateinit var currencySpinner: Spinner
            private lateinit var saveButton: Button

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_settings)

                // Initialize UI components
                currencySpinner = findViewById(R.id.currencySpinner)
                saveButton = findViewById(R.id.saveSettingsButton)

                // Setup currency spinner
                val currencies = arrayOf("$ (USD)", "€ (EUR)", "£ (GBP)", "¥ (JPY)", "Rs (LKR)")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                currencySpinner.adapter = adapter

                // Load current settings
                loadSettings()

                // Set up save button
                saveButton.setOnClickListener {
                    saveSettings()
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

                // Settings is current activity
                findViewById<View>(R.id.navSettings).setOnClickListener {
                    // Already in this activity
                }
            }

            private fun loadSettings() {
                val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
                val currency = sharedPreferences.getString("currency", "$")

                val position = when(currency) {
                    "$" -> 0
                    "€" -> 1
                    "£" -> 2
                    "¥" -> 3
                    "Rs" -> 4
                    else -> 0
                }

                currencySpinner.setSelection(position)
            }

            private fun saveSettings() {
                val currency = when(currencySpinner.selectedItemPosition) {
                    0 -> "$"
                    1 -> "€"
                    2 -> "£"
                    3 -> "¥"
                    4 -> "Rs."
                    else -> "$"
                }

                val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("currency", currency).apply()

                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            }
        }