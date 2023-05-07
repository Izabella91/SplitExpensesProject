package com.example.myapplication

import com.google.firebase.database.PropertyName

data class Balance(
    @PropertyName("friend1") val friend1: Friend,
    @PropertyName("amount") val amount: Double,
    @PropertyName("friend2") val friend2: Friend
)