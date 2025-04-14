package com.example.le_3_1

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.le_3_1.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionViewModel : ViewModel() {
    val transactions = MutableLiveData<MutableList<Transaction>>(mutableListOf())
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "FinanceTrackerPrefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_NOTIFIED_APPROACH = "notified_approach"
        private const val KEY_NOTIFIED_EXCEED = "notified_exceed"
    }

    fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = prefs.getString(KEY_TRANSACTIONS, null)
        if (transactionsJson != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            val loadedTransactions: MutableList<Transaction> = gson.fromJson(transactionsJson, type)
            transactions.postValue(loadedTransactions)
        }
    }

    fun savePreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val transactionsJson = gson.toJson(transactions.value)
        editor.putString(KEY_TRANSACTIONS, transactionsJson)
        editor.apply()
    }

    fun addTransaction(transaction: Transaction, context: Context) {
        val currentList = transactions.value ?: mutableListOf()
        currentList.add(transaction)
        transactions.postValue(currentList)
        savePreferences(context)
        checkBudgetAndNotify(context)
    }

    fun setCurrency(context: Context, currency: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "LKR") ?: "LKR"
    }

    fun setBudget(context: Context, budget: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_BUDGET, budget.toFloat())
            putBoolean(KEY_NOTIFIED_APPROACH, false)
            putBoolean(KEY_NOTIFIED_EXCEED, false)
            apply()
        }
    }

    fun getBudget(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_BUDGET, 0f).toDouble()
    }

    private fun checkBudgetAndNotify(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val budget = getBudget(context)
        if (budget <= 0) return

        val totalExpenses = transactions.value?.filter { it.type == "Expense" }?.sumOf { it.amount } ?: 0.0
        val budgetPercentage = totalExpenses / budget

        var hasNotifiedApproach = prefs.getBoolean(KEY_NOTIFIED_APPROACH, false)
        var hasNotifiedExceed = prefs.getBoolean(KEY_NOTIFIED_EXCEED, false)

        val editor = prefs.edit()

        when {
            !hasNotifiedExceed && totalExpenses >= budget -> {
                NotificationHelper.sendBudgetNotification(
                    context,
                    "Budget Exceeded",
                    "You have exceeded your monthly budget of LKR $budget. Total spent: LKR $totalExpenses."
                )
                editor.putBoolean(KEY_NOTIFIED_EXCEED, true)
                editor.putBoolean(KEY_NOTIFIED_APPROACH, true) // No need to notify approach after exceeding
            }
            !hasNotifiedApproach && budgetPercentage >= 0.9 -> {
                NotificationHelper.sendBudgetNotification(
                    context,
                    "Budget Warning",
                    "You are approaching your monthly budget of LKR $budget. Total spent: LKR $totalExpenses."
                )
                editor.putBoolean(KEY_NOTIFIED_APPROACH, true)
            }
        }
        editor.apply()
    }
}