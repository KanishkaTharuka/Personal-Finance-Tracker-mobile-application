package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
        adapter = TransactionAdapter(mutableListOf())
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@AllTransactionFragment.adapter
        }
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
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

        binding.tvTotalIncome.text = "Income: %.2f".format(totalIncome)
        binding.tvTotalExpenses.text = "Expenses: %.2f".format(totalExpenses)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}