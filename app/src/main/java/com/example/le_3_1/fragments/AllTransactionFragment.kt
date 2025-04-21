package com.example.le_3_1.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.le_3_1.R
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.adapters.TransactionAdapter
import com.example.le_3_1.databinding.FragmentAllTransactionBinding
import com.example.le_3_1.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AllTransactionFragment : Fragment() {

    private var _binding: FragmentAllTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionViewModel
    private val timePeriods = listOf("Today", "This Week", "This Month", "This Year", "All Time")
    private var allTransactions: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        viewModel.loadPreferences(requireContext())
        setupRecyclerView()
        setupTimePeriodSpinner()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            mutableListOf(),
            onEditClick = { transaction -> showEditDialog(transaction) },
            onDeleteClick = { transactionId -> deleteTransaction(transactionId) }
        )
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AllTransactionFragment.adapter
        }
    }

    private fun showEditDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_transaction, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_title)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val etDate = dialogView.findViewById<EditText>(R.id.et_date)

        // Pre-fill the dialog with transaction data
        etAmount.setText(transaction.amount.toString())
        etTitle.setText(transaction.title)
        etDate.setText(transaction.date)

        // Parse the existing date to initialize the DatePicker
        val calendar = Calendar.getInstance()
        try {
            val date = dateFormat.parse(transaction.date)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // If parsing fails, use current date
            calendar.time = Date()
        }

        // Setup DatePicker for the date field
        etDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    etDate.setText(dateFormat.format(calendar.time))
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Setup category spinner
        val categories = if (transaction.type == "Income") {
            listOf("Salary", "Gift")
        } else {
            listOf("Food", "Transport", "Bills", "Other")
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        spinnerCategory.setSelection(categories.indexOf(transaction.category))

        // Apply custom theme to AlertDialog for rounded corners and button colors
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Edit Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull()
                val title = etTitle.text.toString()
                val category = spinnerCategory.selectedItem.toString()
                val date = etDate.text.toString()

                if (amount == null || title.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validate date format (dd/MM/yyyy)
                dateFormat.isLenient = false // Strict parsing
                try {
                    dateFormat.parse(date)
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid date format. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedTransaction = Transaction(
                    id = transaction.id,
                    amount = amount,
                    title = title,
                    category = category,
                    date = date,
                    type = transaction.type
                )
                viewModel.editTransaction(updatedTransaction, requireContext())
                Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTimePeriodSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timePeriods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimePeriod.adapter = adapter
        // Set default selection to "All Time" (index 4)
        binding.spinnerTimePeriod.setSelection(4)

        binding.spinnerTimePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterTransactions(timePeriods[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun deleteTransaction(transactionId: Long) {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteTransaction(transactionId, requireContext())
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            allTransactions = transactions
            // Filter based on the currently selected time period
            val selectedPeriod = timePeriods[binding.spinnerTimePeriod.selectedItemPosition]
            filterTransactions(selectedPeriod)
        }
    }

    private fun filterTransactions(period: String) {
        val calendar = Calendar.getInstance()

        val filteredTransactions = when (period) {
            "Today" -> {
                val today = dateFormat.format(calendar.time)
                allTransactions.filter { it.date == today }
            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val startOfWeek = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = dateFormat.format(calendar.time)
                allTransactions.filter {
                    val transactionDate = dateFormat.parse(it.date)
                    val startDate = dateFormat.parse(startOfWeek)
                    val endDate = dateFormat.parse(endOfWeek)
                    transactionDate != null && startDate != null && endDate != null &&
                            (transactionDate >= startDate && transactionDate <= endDate)
                }
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endOfMonth = dateFormat.format(calendar.time)
                allTransactions.filter {
                    val transactionDate = dateFormat.parse(it.date)
                    val startDate = dateFormat.parse(startOfMonth)
                    val endDate = dateFormat.parse(endOfMonth)
                    transactionDate != null && startDate != null && endDate != null &&
                            (transactionDate >= startDate && transactionDate <= endDate)
                }
            }
            "This Year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val startOfYear = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                val endOfYear = dateFormat.format(calendar.time)
                allTransactions.filter {
                    val transactionDate = dateFormat.parse(it.date)
                    val startDate = dateFormat.parse(startOfYear)
                    val endDate = dateFormat.parse(endOfYear)
                    transactionDate != null && startDate != null && endDate != null &&
                            (transactionDate >= startDate && transactionDate <= endDate)
                }
            }
            "All Time" -> allTransactions
            else -> allTransactions
        }

        adapter.updateTransactions(filteredTransactions)
        updateSummary(filteredTransactions)
    }

    private fun updateSummary(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val currency = viewModel.getCurrency(requireContext())

        binding.tvTotalIncome.text = "Income: $currency %.2f".format(totalIncome)
        binding.tvTotalExpenses.text = "Expenses: $currency %.2f".format(totalExpenses)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}