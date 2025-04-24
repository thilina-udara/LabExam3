package com.example.labexam3

import android.app.Activity
import android.view.View

object BottomNavHelper {
    fun updateSelectedNav(activity: Activity, navItemId: Int) {
        // Reset all navigation items
        activity.findViewById<View>(R.id.navHome)?.isSelected = false
        activity.findViewById<View>(R.id.navTransactions)?.isSelected = false
        activity.findViewById<View>(R.id.navBudget)?.isSelected = false
        activity.findViewById<View>(R.id.navAnalysis)?.isSelected = false
        activity.findViewById<View>(R.id.navSettings)?.isSelected = false

        // Set current one as selected
        activity.findViewById<View>(navItemId)?.isSelected = true
    }
}