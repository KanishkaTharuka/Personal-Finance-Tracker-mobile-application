package com.example.le_3_1.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.le_3_1.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("FinanceTrackerPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CURRENCY = "currency_type"
        private const val KEY_BUDGET = "monthly_budget"
        private const val KEY_TRANSACTIONS = "transaction_history"
    }

    // Save and retrieve currency type
    fun saveCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    // Save and retrieve monthly budget
    fun saveBudget(budget: Float) {
        prefs.edit().putFloat(KEY_BUDGET, budget).apply()
    }

    fun getBudget(): Float {
        return prefs.getFloat(KEY_BUDGET, 0f)
    }

    // Save and retrieve transaction history
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
}