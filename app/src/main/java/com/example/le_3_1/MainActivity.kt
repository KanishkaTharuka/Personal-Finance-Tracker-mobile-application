package com.example.le_3_1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.le_3_1.databinding.ActivityMainBinding
import com.example.le_3_1.fragments.AddTransactionFragment
import com.example.le_3_1.fragments.AllTransactionFragment
//import com.example.le_3_1.fragments.CategoryFragment
//import com.example.le_3_1.fragments.GraphFragment
//import com.example.le_3_1.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val allTransactionFragment = AllTransactionFragment()
//    private val graphFragment = GraphFragment()
    private val addFragment = AddTransactionFragment()
//    private val categoryFragment = CategoryFragment()
//    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(allTransactionFragment)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_all_transaction -> replaceFragment(allTransactionFragment)
//                R.id.menu_graph -> replaceFragment(graphFragment)
                R.id.menu_add -> replaceFragment(addFragment)
//                R.id.menu_category -> replaceFragment(categoryFragment)
//                R.id.menu_profile -> replaceFragment(profileFragment)
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}