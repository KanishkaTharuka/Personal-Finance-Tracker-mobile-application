package com.example.le_3_1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.le_3_1.R
import com.example.le_3_1.databinding.ItemTransactionBinding
import com.example.le_3_1.models.Transaction

class TransactionAdapter(private val transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvAmount.text = if (transaction.type == "Income") "+${transaction.amount}" else "-${transaction.amount}"
            binding.tvTitle.text = transaction.description
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = transaction.date
            binding.tvAmount.setTextColor(
                binding.root.context.getColor(
                    if (transaction.type == "Income") R.color.green else R.color.red
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}