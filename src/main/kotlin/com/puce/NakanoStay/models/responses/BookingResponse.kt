package com.puce.NakanoStay.models.responses

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class BookingResponse(
    val id: Long,
    val userId: Long,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: String,
    val total: BigDecimal,
    val details: List<BookingDetailResponse>
)

