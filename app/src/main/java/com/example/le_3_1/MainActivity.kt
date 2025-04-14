package com.example.le_3_1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.le_3_1.databinding.ActivityMainBinding
import com.example.le_3_1.fragments.AddTransactionFragment
import com.example.le_3_1.fragments.AllTransactionFragment
import com.example.le_3_1.fragments.CategoryFragment
import com.example.le_3_1.fragments.GraphFragment
import com.example.le_3_1.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val allTransactionFragment = AllTransactionFragment()
    private val graphFragment = GraphFragment()
    private val addFragment = AddTransactionFragment()
    private val categoryFragment = CategoryFragment()
    private val profileFragment = ProfileFragment()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.createNotificationChannel(this)
            NotificationHelper.scheduleDailyReminder(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val viewModel = TransactionViewModel()
        if (viewModel.getCurrency(this) == "LKR") {
            viewModel.setCurrency(this, "LKR")
            viewModel.setBudget(this, 10000.0)
        }

        // Request notification permission and initialize notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.createNotificationChannel(this)
                NotificationHelper.scheduleDailyReminder(this)
            }
        } else {
            NotificationHelper.createNotificationChannel(this)
            NotificationHelper.scheduleDailyReminder(this)
        }

        replaceFragment(allTransactionFragment)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_all_transaction -> replaceFragment(allTransactionFragment)
                R.id.menu_graph -> replaceFragment(graphFragment)
                R.id.menu_add -> replaceFragment(addFragment)
                R.id.menu_category -> replaceFragment(categoryFragment)
                R.id.menu_profile -> replaceFragment(profileFragment)
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun updateTheme(isDarkTheme: Boolean) {
        val prefs = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
        // Since Theme.LE_3_1 is a DayNight theme, we can use AppCompatDelegate to switch modes
        val mode = if (isDarkTheme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        } else {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        // Recreate activity to apply the new theme
        recreate()
    }


}