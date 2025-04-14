package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.le_3_1.adapters.CategoryAdapter
import com.example.le_3_1.databinding.FragmentCategoryBinding
import com.example.le_3_1.models.Transaction
import com.example.le_3_1.TransactionViewModel

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        viewModel.loadPreferences(requireContext())
        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        val categories = listOf("Food", "Transport", "Bills", "Salary", "Gift", "Other")
        val adapter = CategoryAdapter(categories, mutableMapOf())
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val categoryExpenses = calculateExpensesByCategory(transactions)
            val adapter = binding.rvCategories.adapter as CategoryAdapter
            adapter.updateExpenses(categoryExpenses)
        }
    }

    private fun calculateExpensesByCategory(transactions: List<Transaction>): Map<String, Double> {
        val categories = listOf("Food", "Transport", "Bills", "Salary", "Gift", "Other")
        val expenses = mutableMapOf<String, Double>().withDefault { 0.0 }
        categories.forEach { category -> expenses[category] = 0.0 }

        transactions.filter { it.type == "Expense" }.forEach { transaction ->
            val currentTotal = expenses.getValue(transaction.category)
            expenses[transaction.category] = currentTotal + transaction.amount
        }

        return expenses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}