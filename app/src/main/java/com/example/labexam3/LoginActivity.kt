package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterPrompt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterPrompt = findViewById(R.id.tvRegisterPrompt)

        // Check if user is already logged in
        checkLoggedInState()

        // Set up login button click listener
        btnLogin.setOnClickListener {
            handleLogin()
        }

        // Set up register prompt click listener
        tvRegisterPrompt.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun checkLoggedInState() {
        val sharedPreferences = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // If user is already logged in, go directly to MainActivity
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Basic validation
        when {
            username.isEmpty() -> {
                etUsername.error = "Username cannot be empty"
                return
            }
            password.isEmpty() -> {
                etPassword.error = "Password cannot be empty"
                return
            }
        }

        // Check credentials against stored credentials
        val sharedPreferences = getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE)
        val storedUsername = sharedPreferences.getString("username", "")
        val storedPassword = sharedPreferences.getString("password", "")

        // First-time login (no stored credentials) or correct credentials
        if ((storedUsername.isNullOrEmpty() && storedPassword.isNullOrEmpty()) ||
            (username == storedUsername && password == storedPassword)) {

            // Save login state and user details if first time
            if (storedUsername.isNullOrEmpty()) {
                sharedPreferences.edit().apply {
                    putString("username", username)
                    putString("password", password)
                    putBoolean("isLoggedIn", true)
                    apply()
                }
            } else {
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
            }

            // Navigate to main activity
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null)
        val etRegisterUsername = dialogView.findViewById<EditText>(R.id.etRegisterUsername)
        val etRegisterPassword = dialogView.findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Register New Account")
            .setView(dialogView)
            .setPositiveButton("Register") { _, _ ->
                val username = etRegisterUsername.text.toString().trim()
                val password = etRegisterPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (validateRegistration(username, password, confirmPassword)) {
                    // Save credentials to SharedPreferences
                    getSharedPreferences("CoinomyUserPrefs", MODE_PRIVATE).edit().apply {
                        putString("username", username)
                        putString("password", password)
                        apply()
                    }

                    Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show()

                    // Auto-fill the username field
                    etUsername.setText(username)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateRegistration(username: String, password: String, confirmPassword: String): Boolean {
        when {
            username.isEmpty() -> {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }
}