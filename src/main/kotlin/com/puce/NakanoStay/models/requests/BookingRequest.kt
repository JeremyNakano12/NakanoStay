package com.puce.NakanoStay.models.requests

import java.time.LocalDate

data class BookingRequest(
    val userId: Long,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: String = "pending",
    val details: List<BookingDetailRequest>
)


