package com.puce.NakanoStay.models.responses

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class HotelResponse(
    val id: Long,
    val name: String,
    val address: String,
    val city: String?,
    val stars: Int?,
    val email: String
)
