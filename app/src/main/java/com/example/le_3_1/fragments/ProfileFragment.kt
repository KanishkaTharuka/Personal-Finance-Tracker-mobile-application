package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.le_3_1.MainActivity
import com.example.le_3_1.NotificationHelper
import com.example.le_3_1.R
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Set profile initial from username
        val username = "ashantharuka2001"
        binding.tvUsername.text = username
        binding.tvEmail.text = "ashantharuka2001@gmail.com"
        binding.tvProfileInitial.text = username.first().toString().uppercase()

        // Load daily reminder preference
        val prefs = requireContext().getSharedPreferences("FinanceTrackerPrefs", 0)
        val dailyReminderEnabled = prefs.getBoolean("daily_reminder", false)
        binding.switchDailyReminder.isChecked = dailyReminderEnabled

        // Load theme preference
        val isDarkTheme = prefs.getBoolean("is_dark_theme", false)
        binding.switchTheme.isChecked = isDarkTheme
    }

    private fun setupListeners() {
        binding.optionLanguage.setOnClickListener {
            Toast.makeText(context, "Language selection not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.optionCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        binding.optionBudget.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, BudgetFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.optionChangePassword.setOnClickListener {
            Toast.makeText(context, "Change Password not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.optionFeedback.setOnClickListener {
            Toast.makeText(context, "Feedback not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.optionRateSupport.setOnClickListener {
            Toast.makeText(context, "Rate and Support not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.optionSettings.setOnClickListener {
            Toast.makeText(context, "Settings not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            val prefs = requireContext().getSharedPreferences("FinanceTrackerPrefs", 0)
            prefs.edit().putBoolean("daily_reminder", isChecked).apply()
            NotificationHelper.scheduleDailyReminder(requireContext())
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            (activity as? MainActivity)?.updateTheme(isChecked)
        }
    }

    private fun showCurrencyDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_currency, null)
        val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_dialog_currency)

        val currencies = listOf("LKR", "USD", "EUR", "GBP")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentCurrency = viewModel.getCurrency(requireContext())
        spinner.setSelection(currencies.indexOf(currentCurrency))

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Currency")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selectedCurrency = spinner.selectedItem.toString()
                viewModel.setCurrency(requireContext(), selectedCurrency)
                Toast.makeText(context, "Currency updated to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}