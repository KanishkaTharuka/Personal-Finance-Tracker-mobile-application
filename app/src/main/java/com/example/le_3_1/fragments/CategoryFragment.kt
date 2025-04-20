package com.example.le_3_1.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.le_3_1.R
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.adapters.Category
import com.example.le_3_1.adapters.CategoryAdapter
import com.example.le_3_1.databinding.FragmentCategoryBinding
import com.example.le_3_1.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryAdapter
    private lateinit var viewModel: TransactionViewModel
    private val timePeriods = listOf("Today", "This Week", "This Month", "This Year", "All Time")
    private var allTransactions: List<Transaction> = emptyList()

    // Colors for the bar chart segments (matching the screenshot)
    private val categoryColors = mapOf(
        "Gifts" to R.color.pink,   // Purple for Gifts
        "Food" to R.color.blue,      // Blue for Food
        "Transport" to R.color.red,  // Red for Transport (as per your code)
        "Bills" to R.color.ligt_purple,    // Green for Bills
        "Salary" to R.color.yellow,    // Gray for Salary
        "Other" to R.color.gray // Blue for Other
    )

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
        setupTimePeriodSpinner()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(mutableListOf())
        binding.rvCategories.adapter = adapter
        // GridLayoutManager is already set in XML with 2 columns
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

        updateCategories(filteredTransactions)
    }

    private fun updateCategories(transactions: List<Transaction>) {
        // Define all possible categories
        val allCategories = listOf("Food", "Transport", "Gift", "Other", "Bills", "Salary")

        // Calculate total expenses for each category
        val expenseCategories = allCategories.map { categoryName ->
            val totalExpense = transactions
                .filter { it.category == categoryName && it.type == "Expense" }
                .sumOf { it.amount }
            Category(categoryName, totalExpense)
        }

        // Calculate total income for each category
        val incomeCategories = allCategories.map { categoryName ->
            val totalIncome = transactions
                .filter { it.category == categoryName && it.type == "Income" }
                .sumOf { it.amount }
            Category(categoryName, totalIncome)
        }

        // Update RecyclerView with combined categories (limited to 6 items for 2 columns, 3 rows)
        val combinedCategories = allCategories.map { categoryName ->
            val totalAmount = transactions
                .filter { it.category == categoryName }
                .sumOf { it.amount }
            Category(categoryName, totalAmount)
        }.take(6)
        adapter.updateCategories(combinedCategories)

        // Update summary bar charts and legends for expenses and income
        updateSummary(expenseCategories, incomeCategories)
    }

    private fun updateSummary(expenseCategories: List<Category>, incomeCategories: List<Category>) {
        // --- Expenses Bar Chart ---
        val totalExpenses = expenseCategories.sumOf { it.totalExpense }
        if (totalExpenses == 0.0) {
            binding.barChartExpenses.removeAllViews()
            binding.legendExpenses.removeAllViews()
        } else {
            // Filter categories with non-zero expenses
            val expenseCategoriesWithValues = expenseCategories
                .filter { it.totalExpense > 0 }
                .sortedByDescending { it.totalExpense }
                .take(4) // Limit to top 4 categories

            // Calculate percentages for expenses
            val expensePercentages = expenseCategoriesWithValues.map {
                it to (it.totalExpense / totalExpenses * 100)
            }

            // Update bar chart for expenses
            binding.barChartExpenses.removeAllViews()
            expensePercentages.forEachIndexed { index, (category, percentage) ->
                val barSegment = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        percentage.toFloat()
                    ).apply {
                        if (index < expensePercentages.size - 1) {
                            marginEnd = 4.dpToPx()
                        }
                    }
                    background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bar_segment_background)?.apply {
                        setTint(ContextCompat.getColor(requireContext(), categoryColors[category.name] ?: R.color.gray))
                    }
                }
                binding.barChartExpenses.addView(barSegment)
            }

            // Update legend for expenses
            binding.legendExpenses.removeAllViews()
            expensePercentages.forEach { (category, percentage) ->
                val legendItem = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER

                    // Color square
                    val colorSquare = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            16.dpToPx(),
                            16.dpToPx()
                        )
                        background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.bar_segment_background)?.apply {
                            setTint(ContextCompat.getColor(requireContext(), categoryColors[category.name] ?: R.color.gray))
                        }
                    }
                    addView(colorSquare)

                    // Category name and percentage
                    val textView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginStart = 4.dpToPx()
                        }
                        text = "${category.name} ${"%.1f".format(percentage)}%"
                        textSize = 14f
                    }
                    addView(textView)
                }
                binding.legendExpenses.addView(legendItem)
            }
        }

        // --- Income Bar Chart ---
        val totalIncome = incomeCategories.sumOf { it.totalExpense }
        if (totalIncome == 0.0) {
            binding.barChartIncome.removeAllViews()
            binding.legendIncome.removeAllViews()
        } else {
            // Filter categories with non-zero income
            val incomeCategoriesWithValues = incomeCategories
                .filter { it.totalExpense > 0 }
                .sortedByDescending { it.totalExpense }
                .take(4) // Limit to top 4 categories

            // Calculate percentages for income
            val incomePercentages = incomeCategoriesWithValues.map {
                it to (it.totalExpense / totalIncome * 100)
            }

            // Update bar chart for income
            binding.barChartIncome.removeAllViews()
            incomePercentages.forEachIndexed { index, (category, percentage) ->
                val barSegment = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        percentage.toFloat()
                    ).apply {
                        if (index < incomePercentages.size - 1) {
                            marginEnd = 4.dpToPx()
                        }
                    }
                    background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bar_segment_background)?.apply {
                        setTint(ContextCompat.getColor(requireContext(), categoryColors[category.name] ?: R.color.gray))
                    }
                }
                binding.barChartIncome.addView(barSegment)
            }

            // Update legend for income
            binding.legendIncome.removeAllViews()
            incomePercentages.forEach { (category, percentage) ->
                val legendItem = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER

                    // Color square
                    val colorSquare = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            16.dpToPx(),
                            16.dpToPx()
                        )
                        background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.bar_segment_background)?.apply {
                            setTint(ContextCompat.getColor(requireContext(), categoryColors[category.name] ?: R.color.gray))
                        }
                    }
                    addView(colorSquare)

                    // Category name and percentage
                    val textView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginStart = 4.dpToPx()
                        }
                        text = "${category.name} ${"%.1f".format(percentage)}%"
                        textSize = 14f
                    }
                    addView(textView)
                }
                binding.legendIncome.addView(legendItem)
            }
        }
    }

    // Utility to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}