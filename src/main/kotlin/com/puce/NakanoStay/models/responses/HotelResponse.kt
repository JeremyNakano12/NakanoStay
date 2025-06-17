package com.puce.NakanoStay.models.responses

data class HotelResponse(
    val id: Long,
    val name: String,
    val address: String,
    val city: String?,
    val stars: Int?
)
