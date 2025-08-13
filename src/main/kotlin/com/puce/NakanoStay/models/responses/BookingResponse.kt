package com.puce.NakanoStay.models.responses

import com.puce.NakanoStay.models.enums.BookingStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class BookingResponse(
    val id: Long,
    val bookingCode: String,
    val guestName: String,
    val guestDni: String,
    val guestEmail: String,
    val guestPhone: String?,
    val bookingDate: LocalDateTime,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: BookingStatus,
    val total: BigDecimal,
    val details: List<BookingDetailResponse>
)