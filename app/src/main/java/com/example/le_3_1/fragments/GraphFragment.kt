package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.databinding.FragmentGraphBinding

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        viewModel.loadPreferences(requireContext())
        updateTotals()
    }

    private fun updateTotals() {
        val transactions = viewModel.transactions.value ?: emptyList()
        val currency = viewModel.getCurrency(requireContext())

        val totalIncome = transactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }
        val totalExpenses = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }

        binding.tvTotalIncome.text = "LKR $totalIncome"
        binding.tvTotalExpenses.text = "LKR $totalExpenses"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}