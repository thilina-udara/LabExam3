package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    private val onboardingData = listOf(
        OnboardingItem(
            R.drawable.onborad1, // Changed to use your login image
            "Track Your Finances",
            "Record your income and expenses easily to keep track of your money flow"
        ),
        OnboardingItem(
            R.drawable.onborad2, // Changed to use your login image
            "Set and Manage Budgets",
            "Create budgets for different categories to control your spending"
        ),
        OnboardingItem(
            R.drawable.onboard3, // Changed to use your login image
            "Analyze Your Spending",
            "Get insights on your financial habits with clear visualizations"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.onboardingViewPager)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)

        viewPager.adapter = OnboardingAdapter()

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                btnNext.text = if (position == onboardingData.size - 1) "Get Started" else "Next"
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingData.size - 1) {
                viewPager.currentItem++
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences("CoinomyPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("isFirstRun", false).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    data class OnboardingItem(
        val imageResId: Int,
        val title: String,
        val description: String
    )

    inner class OnboardingAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.layout_onboarding_page, container, false)

            val imageView = view.findViewById<ImageView>(R.id.imgOnboarding)
            val titleView = view.findViewById<TextView>(R.id.titleOnboarding)
            val descView = view.findViewById<TextView>(R.id.descOnboarding)

            val item = onboardingData[position]
            imageView.setImageResource(item.imageResId)
            titleView.text = item.title
            descView.text = item.description

            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun getCount(): Int = onboardingData.size

        override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj
    }
}