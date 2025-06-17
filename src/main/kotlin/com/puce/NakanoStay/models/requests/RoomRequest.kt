package com.puce.NakanoStay.models.requests

import java.math.BigDecimal

data class RoomRequest(
    val hotelId: Long,
    val roomNumber: String,
    val roomType: String?,
    val pricePerNight: BigDecimal,
    val isAvailable: Boolean
)
