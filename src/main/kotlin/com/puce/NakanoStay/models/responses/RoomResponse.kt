package com.puce.NakanoStay.models.responses

import java.math.BigDecimal

data class RoomResponse(
    val id: Long,
    val hotelId: Long,
    val roomNumber: String,
    val roomType: String?,
    val pricePerNight: BigDecimal,
    val isAvailable: Boolean
)
