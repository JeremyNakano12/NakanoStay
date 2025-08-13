package com.puce.NakanoStay.models.requests

import java.math.BigDecimal

data class BookingDetailRequest(
    val roomId: Long,
    val guests: Int
)