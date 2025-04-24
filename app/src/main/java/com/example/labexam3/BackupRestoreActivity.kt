package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var backupButton: Button
    private lateinit var restoreButton: Button
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)

        // Initialize UI components
        backupButton = findViewById(R.id.backupButton)
        restoreButton = findViewById(R.id.restoreButton)
        statusTextView = findViewById(R.id.statusTextView)

        // Setup button click listeners
        backupButton.setOnClickListener {
            createBackup()
        }

        restoreButton.setOnClickListener {
            showBackupsList()
        }

        // Setup navigation
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

    private fun createBackup() {
        val transactions = getAllTransactions()
        if (transactions.length() == 0) {
            Toast.makeText(this, "No transactions to backup", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Create backup directory if it doesn't exist
            val backupDir = File(filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdir()
            }

            // Generate unique filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "coinomy_backup_$timestamp.json")

            // Write transactions to backup file
            backupFile.writeText(transactions.toString())

            statusTextView.text = "Backup created successfully!"
            Toast.makeText(this, "Backup created successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            statusTextView.text = "Backup failed: ${e.message}"
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBackupsList() {
        val backupDir = File(filesDir, "backups")
        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, "No backups found", Toast.LENGTH_SHORT).show()
            return
        }

        val backupFiles = backupDir.listFiles()?.filter { it.name.endsWith(".json") }
        if (backupFiles.isNullOrEmpty()) {
            Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show()
            return
        }

        // Sort files by last modified date (newest first)
        val sortedFiles = backupFiles.sortedByDescending { it.lastModified() }
        val fileNames = sortedFiles.map {
            val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .parse(it.name.replace("coinomy_backup_", "").replace(".json", ""))
            if (date != null) {
                SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(date)
            } else {
                it.name
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select a Backup to Restore")
            .setItems(fileNames) { _, which ->
                confirmRestoreBackup(sortedFiles[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmRestoreBackup(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore this backup? This will replace all your current transaction data.")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(backupFile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restoreBackup(backupFile: File) {
        try {
            val backupData = backupFile.readText()

            // Validate JSON structure
            try {
                val jsonArray = JSONArray(backupData)

                // Simple validation - check if each item is a valid transaction
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    if (!item.has("type") || !item.has("amount") || !item.has("date")) {
                        throw JSONException("Invalid transaction format in backup file")
                    }
                }

                // Backup is valid, save it
                val transactionFile = File(filesDir, "transactions.json")
                transactionFile.writeText(backupData)

                statusTextView.text = "Backup restored successfully!"
                Toast.makeText(this, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                statusTextView.text = "Invalid backup file format"
                Toast.makeText(this, "Invalid backup file format", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            statusTextView.text = "Failed to restore backup: ${e.message}"
            Toast.makeText(this, "Failed to restore backup", Toast.LENGTH_SHORT).show()
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
}