package com.example.servify
data class Orders1(
    var id: String? = null,  // This holds the orderId
    val serviceName: String? = null,
    val userName: String? = null,
    val userContact: String? = null,
    val userAddress: String? = null,
    val paymentMethod: String? = null,
    val utr: String? = null,
    val completionStatus: String? = null,
    val price: String? = null,
    val serviceAddress: String? = null,
    val serviceProviderContact: String? = null
)