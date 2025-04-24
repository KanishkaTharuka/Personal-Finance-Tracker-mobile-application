package com.example.le_3_1

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.le_3_1.databinding.ActivityMainBinding
import com.example.le_3_1.fragments.AddTransactionFragment
import com.example.le_3_1.fragments.AllTransactionFragment
import com.example.le_3_1.fragments.CategoryFragment
import com.example.le_3_1.fragments.GraphFragment
import com.example.le_3_1.fragments.ProfileFragment

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Your animation code (example: fade out)
            ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                View.ALPHA,
                1f,
                0f
            ).apply {
                duration = 500L
                interpolator = AccelerateInterpolator()
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }

        lifecycleScope.launch {
            delay(1000) // Simulate data loading
            keepSplashOnScreen = false // Allow splash screen to hide
        }



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
            // Reset styles for the BottomNavigationView
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