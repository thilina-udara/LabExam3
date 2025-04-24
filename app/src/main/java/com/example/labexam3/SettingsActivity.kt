package com.example.labexam3

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {
    // UI Components
    private lateinit var profileImageView: ImageView
    private lateinit var changeProfileButton: ImageView
    private lateinit var displayEmailText: TextView
    private lateinit var displayUsernameText: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var currencySpinner: Spinner
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var signOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI components
        initializeViews()

        // Setup click listeners
        setupListeners()

        // Setup currency spinner
        setupCurrencySpinner()

        // Load current settings
        loadSettings()

        // Set up navigation
        setupNavigation()

        // Highlight the Settings navigation item
        BottomNavHelper.updateSelectedNav(this, R.id.navSettings)
    }

    override fun onResume() {
        super.onResume()

        // Re-highlight the Settings navigation item when returning to this activity
        BottomNavHelper.updateSelectedNav(this, R.id.navSettings)
    }

    private fun initializeViews() {
        profileImageView = findViewById(R.id.profileImageView)
        changeProfileButton = findViewById(R.id.changeProfileButton)
        displayEmailText = findViewById(R.id.displayEmailText)
        displayUsernameText = findViewById(R.id.displayUsernameText)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        currencySpinner = findViewById(R.id.currencySpinner)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        saveButton = findViewById(R.id.saveSettingsButton)
        signOutButton = findViewById(R.id.signOutButton)
    }

    private fun setupListeners() {
        // Profile image click listener
        changeProfileButton.setOnClickListener {
            openImagePicker()
        }

        // Change password button
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        // Save button
        saveButton.setOnClickListener {
            saveAllSettings()
        }

        // Sign out button
        signOutButton.setOnClickListener {
            showSignOutConfirmationDialog()
        }
    }

    private fun showSignOutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        // Clear login state
        val userPrefs = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)
        userPrefs.edit().putBoolean("isLoggedIn", false).apply()

        // Optional: Clear sensitive data if needed
        // userPrefs.edit().remove("password").apply()

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("$ (USD)", "€ (EUR)", "£ (GBP)", "¥ (JPY)", "Rs (LKR)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter
    }

    private fun loadSettings() {
        // Load app preferences
        val appPrefs = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val userPrefs = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)

        // Load profile image
        val profileImageUri = appPrefs.getString("profileImageUri", null)
        if (!profileImageUri.isNullOrEmpty()) {
            try {
                profileImageView.setImageURI(Uri.parse(profileImageUri))
            } catch (e: Exception) {
                // If there's an error, use the placeholder
            }
        }

        // Load user info
        val username = userPrefs.getString("username", "")
        val email = userPrefs.getString("gmail", "")
        displayUsernameText.text = "@$username"
        displayEmailText.text = email
        usernameEditText.setText(username)
        emailEditText.setText(email)

        // Load currency
        val currency = appPrefs.getString("currency", "$")
        val position = when(currency) {
            "$" -> 0
            "€" -> 1
            "£" -> 2
            "¥" -> 3
            "Rs" -> 4
            else -> 0
        }
        currencySpinner.setSelection(position)

        // Load theme preference
        val theme = appPrefs.getString("theme", "system")
        when(theme) {
            "light" -> themeRadioGroup.check(R.id.lightThemeRadio)
            "dark" -> themeRadioGroup.check(R.id.darkThemeRadio)
            else -> themeRadioGroup.check(R.id.systemThemeRadio)
        }
    }

    private fun saveAllSettings() {
        val appPrefs = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        val userPrefs = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)

        // Save username
        val username = usernameEditText.text.toString().trim()
        if (username.isNotEmpty()) {
            userPrefs.edit().putString("username", username).apply()
            displayUsernameText.text = "@$username"
        }

        // Save currency
        val currency = when(currencySpinner.selectedItemPosition) {
            0 -> "$"
            1 -> "€"
            2 -> "£"
            3 -> "¥"
            4 -> "Rs"
            else -> "$"
        }
        appPrefs.edit().putString("currency", currency).apply()

        // Save theme preference
        val theme = when(themeRadioGroup.checkedRadioButtonId) {
            R.id.lightThemeRadio -> "light"
            R.id.darkThemeRadio -> "dark"
            else -> "system"
        }
        appPrefs.edit().putString("theme", theme).apply()
        applyTheme(theme)

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun applyTheme(theme: String) {
        // Would implement actual theme change here
        // This requires AppCompatDelegate.setDefaultNightMode()
        // which you could implement later
    }

    private fun showChangePasswordDialog() {
        // Fix: Use layoutInflater to inflate the view
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        // Get references to EditText fields
        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView) // Pass View object to resolve ambiguity
            .setPositiveButton("Save", null) // Set null to override default behavior
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (validateAndChangePassword(
                    currentPasswordEditText.text.toString(),
                    newPasswordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )) {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun validateAndChangePassword(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        val userPrefs = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)
        val storedPassword = userPrefs.getString("password", "")

        if (current != storedPassword) {
            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new.length < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != confirm) {
            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
            return false
        }

        // Save the new password
        userPrefs.edit().putString("password", new).apply()
        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)

            // Save the selected image URI
            if (selectedImageUri != null) {
                saveProfileImageUri(selectedImageUri.toString())
            }
        }
    }

    private fun saveProfileImageUri(uriString: String) {
        val sharedPreferences = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("profileImageUri", uriString).apply()
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

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}