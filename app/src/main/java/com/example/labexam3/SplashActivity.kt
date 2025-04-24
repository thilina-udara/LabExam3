package com.example.labexam3

    import android.content.Intent
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import androidx.appcompat.app.AppCompatActivity

    class SplashActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash)

            // Delay for 2 seconds then check if user needs onboarding
            Handler(Looper.getMainLooper()).postDelayed({
                checkFirstRun()
            }, 2000)
        }

        private fun checkFirstRun() {
            val prefs = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)

            // Force showing onboarding screen by setting isFirstRun to true
            // Remove this line when you want normal behavior
            prefs.edit().putBoolean("isFirstRun", true).apply()

            val isFirstRun = prefs.getBoolean("isFirstRun", true)

            // Determine where to go next
            if (isFirstRun) {
                // First-time user: show onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
            } else {
                // Returning user: go to login
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish() // Close the splash activity
        }
    }