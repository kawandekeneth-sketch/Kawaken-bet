package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val username: String = "kawaken",
    val balance: Double = 100000.0, // Initial 100k Shillings
    val preferredCurrency: String = "UGX", // UGX or KES
    val mobileNumber: String = "+256 772 000 001", // User mobile number
    val mobileProvider: String = "MTN MoMo" // MTN MoMo, Airtel Money, M-Pesa
)
