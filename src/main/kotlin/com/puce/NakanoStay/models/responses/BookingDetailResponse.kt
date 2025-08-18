package com.puce.NakanoStay.models.responses

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BookingDetailResponse(
    val roomId: Long,
    val guests: Int,
    val priceAtBooking: BigDecimal
)