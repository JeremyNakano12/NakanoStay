package com.puce.NakanoStay.models.requests

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class HotelRequest(
    val name: String,
    val address: String,
    val city: String? = null,
    val stars: Int? = null,
    val email: String
)
