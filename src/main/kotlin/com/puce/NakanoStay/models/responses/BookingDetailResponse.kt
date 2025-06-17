package com.puce.NakanoStay.models.responses

import java.math.BigDecimal

data class BookingDetailResponse(
    val roomId: Long,
    val guests: Int,
    val priceAtBooking: BigDecimal
)