package com.puce.NakanoStay.models.responses

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RoomResponse(
    val id: Long,
    val hotelId: Long,
    val roomNumber: String,
    val roomType: String?,
    val pricePerNight: BigDecimal,
    val isAvailable: Boolean
)
