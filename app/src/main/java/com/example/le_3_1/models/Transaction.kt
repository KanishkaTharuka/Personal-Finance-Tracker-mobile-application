package com.example.le_3_1.models

data class Transaction(
    val id: Long,  // Unique ID for each transaction
    val amount: Double,
    val title: String,
    val category: String,
    val date: String,
    val type: String
)