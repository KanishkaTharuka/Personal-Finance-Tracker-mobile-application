package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.le_3_1.R
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.databinding.FragmentAddTransactionBinding
import com.example.le_3_1.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private var isIncome = true
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val categories = listOf("Food", "Transport", "Bills", "Salary", "Gift", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(Date()))
    }

    private fun setupListeners() {
        binding.btnToggleType.setOnClickListener {
            isIncome = !isIncome
            binding.btnToggleType.text = if (isIncome) "Income" else "Expense"
            binding.btnToggleType.setBackgroundColor(
                if (isIncome) requireContext().getColor(R.color.green) else requireContext().getColor(R.color.red)
            )
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveTransaction()
            }
        }
    }

    private fun validateInput(): Boolean {
        val amount = binding.etAmount.text.toString()
        val title = binding.etTitle.text.toString()

        return when {
            amount.isEmpty() -> {
                Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
                false
            }
            title.isEmpty() -> {
                Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etTitle.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val date = binding.etDate.text.toString()
        val type = if (isIncome) "Income" else "Expense"

        val transaction = Transaction(amount, description, category, date, type)
        viewModel.addTransaction(transaction)

        Toast.makeText(context, "Saved: $type - $amount - $category - $description", Toast.LENGTH_LONG).show()
        clearForm()
    }

    private fun clearForm() {
        binding.etAmount.text.clear()
        binding.etTitle.text.clear()
        binding.spinnerCategory.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}