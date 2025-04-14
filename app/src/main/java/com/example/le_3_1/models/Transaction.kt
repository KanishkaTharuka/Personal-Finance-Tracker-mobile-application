package com.example.le_3_1.models

data class Transaction(
    val amount: Double,
    val title: String,
    val category: String,
    val date: String,
    val type: String
)