package com.example.le_3_1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.le_3_1.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<String>,
    private val expenses: MutableMap<String, Double>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, expense: Double) {
            binding.tvCategory.text = category
            binding.tvExpense.text = "LKR $expense"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val expense = expenses.getOrDefault(category,0.0)
        holder.bind(category, expense)

    }

    override fun getItemCount(): Int = categories.size

    fun updateExpenses(newExpenses: Map<String, Double>) {
        expenses.clear()
        expenses.putAll(newExpenses)
        notifyDataSetChanged()
    }
}