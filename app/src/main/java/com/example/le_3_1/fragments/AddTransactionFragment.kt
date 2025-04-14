package com.example.le_3_1.fragments

import android.graphics.Typeface
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

    private var isIncome = false
    private lateinit var viewModel: TransactionViewModel
    private val incomeCategories = listOf("Salary", "Gift", "Other")
    private val expenseCategories = listOf("Food", "Transport", "Bills", "Other")

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
        updateCategorySpinner()
        updateButtonStyles()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(Date()))
    }

    private fun updateCategorySpinner() {
        val categories = if (isIncome) incomeCategories else expenseCategories
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun updateButtonStyles() {
        if (isIncome) {
            //binding.btnIncome.setBackgroundResource(R.drawable.button_income_selected)
//            binding.btnIncome.setTextColor(requireContext().getColor(R.color.white))
            binding.btnExpense.setBackgroundResource(R.drawable.button_expense_unselected)
//            binding.btnExpense.setTextColor(requireContext().getColor(R.color.black))

            binding.btnIncome.setTypeface(null, Typeface.BOLD)
            binding.btnExpense.setTypeface(null, Typeface.NORMAL)
            binding.btnExpense.alpha = 0.5f
            binding.btnIncome.alpha = 1.0f

        } else {
           //binding.btnExpense.setBackgroundResource(R.drawable.button_expense_selected)
//            binding.btnExpense.setTextColor(requireContext().getColor(R.color.white))
            binding.btnIncome.setBackgroundResource(R.drawable.button_income_unselected)
//            binding.btnIncome.setTextColor(requireContext().getColor(R.color.black))

            binding.btnExpense.setTypeface(null, Typeface.BOLD)
            binding.btnIncome.setTypeface(null, Typeface.NORMAL)
            binding.btnIncome.alpha = 0.5f
            binding.btnExpense.alpha = 1.0f
        }
    }

    private fun setupListeners() {
        binding.btnExpense.setOnClickListener {
            isIncome = false
            updateButtonStyles()
            updateCategorySpinner()
        }

        binding.btnIncome.setOnClickListener {
            isIncome = true
            updateButtonStyles()
            updateCategorySpinner()
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
                Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val title = binding.etTitle.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val date = binding.etDate.text.toString()
        val type = if (isIncome) "Income" else "Expense"

        val transaction = Transaction(amount, title, category, date, type)
        viewModel.addTransaction(transaction, requireContext())

        Toast.makeText(context, "Saved: $type - $amount - $category - $title", Toast.LENGTH_LONG).show()
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