package com.example.le_3_1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.le_3_1.TransactionViewModel
import com.example.le_3_1.adapters.TransactionAdapter
import com.example.le_3_1.databinding.FragmentAllTransactionBinding

class AllTransactionFragment : Fragment() {

    private var _binding: FragmentAllTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(mutableListOf())
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@AllTransactionFragment.adapter
        }
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}