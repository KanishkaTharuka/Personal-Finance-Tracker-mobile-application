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
import com.example.le_3_1.models.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

        // Export Data Button
        binding.optionExportData.setOnClickListener {
            exportData()
        }

        // Import Data Button
        binding.optionImportData.setOnClickListener {
            importData()
        }
    }

    private fun exportData() {
        try {
            // Get all transactions from ViewModel
            val transactions = viewModel.transactions.value ?: emptyList()
            if (transactions.isEmpty()) {
                Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert transactions to JSON
            val jsonArray = JSONArray()
            transactions.forEach { transaction ->
                val jsonObject = JSONObject().apply {
                    put("id", transaction.id)
                    put("amount", transaction.amount)
                    put("title", transaction.title)
                    put("category", transaction.category)
                    put("date", transaction.date)
                    put("type", transaction.type)
                }
                jsonArray.put(jsonObject)
            }

            // Write JSON to a file in internal storage
            val fileName = "transaction_backup.json"
            val file = File(requireContext().filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonArray.toString().toByteArray())
            }

            Toast.makeText(context, "Data exported to $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun importData() {
        try {
            // Read the backup file from internal storage
            val fileName = "transaction_backup.json"
            val file = File(requireContext().filesDir, fileName)

            if (!file.exists()) {
                Toast.makeText(context, "No backup file found", Toast.LENGTH_SHORT).show()
                return
            }

            // Read JSON from file
            val jsonString = FileInputStream(file).use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }

            // Parse JSON and convert to transactions
            val jsonArray = JSONArray(jsonString)
            val transactions = mutableListOf<Transaction>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val transaction = Transaction(
                    id = jsonObject.getLong("id"),
                    amount = jsonObject.getDouble("amount"),
                    title = jsonObject.getString("title"),
                    category = jsonObject.getString("category"),
                    date = jsonObject.getString("date"),
                    type = jsonObject.getString("type")
                )
                transactions.add(transaction)
            }

            // Add transactions to ViewModel (this will update the database)
            transactions.forEach { transaction ->
                viewModel.addTransaction(transaction, requireContext())
            }

            Toast.makeText(context, "Data imported successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to import data: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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

        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
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