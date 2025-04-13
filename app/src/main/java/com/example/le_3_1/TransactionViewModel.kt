package com.example.le_3_1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.le_3_1.models.Transaction

class TransactionViewModel : ViewModel() {
    val transactions = MutableLiveData<MutableList<Transaction>>(mutableListOf())

    fun addTransaction(transaction: Transaction) {
        val currentList = transactions.value ?: mutableListOf()
        currentList.add(transaction)
        transactions.postValue(currentList)
    }
}