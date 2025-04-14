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
    }

    private fun setupUI() {
        val budget = viewModel.getBudget(requireContext())
        binding.etBudget.setText(budget.toString())
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveBudget()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}