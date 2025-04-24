package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.databinding.FragmentBudgetBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        setupUI()
        setupListeners()
        updateProgressBar()
    }

    private fun setupUI() {
        val budget = viewModel.getBudget(requireContext())
        binding.etBudget.setText(budget.toString())
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveBudget()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // Go back to the previous fragment
        }
    }

    private fun saveBudget() {
        val budgetText = binding.etBudget.text.toString()

        if (budgetText.isEmpty()) {
            Toast.makeText(context, "Please enter a budget", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = budgetText.toDoubleOrNull()
        if (budget == null || budget < 0) {
            Toast.makeText(context, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.setBudget(requireContext(), budget)
        Toast.makeText(context, "Budget updated to LKR $budget", Toast.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    private fun updateProgressBar() {
        // Get the current budget
        val budget = viewModel.getBudget(requireContext())

        // Get all transactions and calculate total expenses for the current month
        val transactions = viewModel.transactions.value ?: emptyList()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val totalExpenses = transactions
            .filter { transaction ->
                val transactionDate = SimpleDateFormat("dd/MM/yyyy").parse(transaction.date)
                val transactionCalendar = Calendar.getInstance().apply { time = transactionDate }
                transaction.type == "Expense" &&
                        transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                        transactionCalendar.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }

        // Calculate percentage (avoid division by zero)
        val percentage = if (budget > 0) {
            ((totalExpenses / budget) * 100).coerceIn(0.0, 100.0).toInt()
        } else {
            0
        }

        // Update ProgressBar and TextView
        binding.progressBar.progress = percentage
        binding.tvProgressPercentage.text = "Expenses: $percentage% of Budget (LKR $totalExpenses / $budget)"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}