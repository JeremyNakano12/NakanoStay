package com.puce.NakanoStay.models.requests

data class HotelRequest(
    val name: String,
    val address: String,
    val city: String? = null,
    val stars: Int? = null,
    val email: String
)
