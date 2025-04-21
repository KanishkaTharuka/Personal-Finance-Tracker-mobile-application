package com.example.le_3_1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.le_3_1.R
import com.example.le_3_1.databinding.ItemTransactionBinding
import com.example.le_3_1.models.Transaction

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Long) -> Unit
    ) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemTransactionBinding,
        private val onEditClick: (Transaction) -> Unit,
        private val onDeleteClick: (Long) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val currency = binding.root.context.getSharedPreferences("FinanceTrackerPrefs", 0)
                .getString("currency", "LKR") ?: "LKR"
            binding.tvAmount.text = if (transaction.type == "Income") "+$currency${transaction.amount}" else "-$currency${transaction.amount}"
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = transaction.date
            binding.tvAmount.setTextColor(
                binding.root.context.getColor(
                    if (transaction.type == "Income") R.color.green else R.color.red
                )
            )
            binding.btnEdit.setOnClickListener {
                onEditClick(transaction)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(transaction.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onEditClick, onDeleteClick)
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