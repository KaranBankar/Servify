package com.example.servify

data class Order(
    val completionStatus: String? = null,
    val paymentMethod: String? = null,
    val price: String? = null,
    val serviceAddress: String? = null,
    val serviceName: String? = null,
    val serviceProviderContact: String? = null,
    val userAddress: String? = null,
    val userContact: String? = null,
    val userName: String? = null,
    val utr: String? = null
)
