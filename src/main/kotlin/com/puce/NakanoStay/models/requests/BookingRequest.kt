package com.puce.NakanoStay.models.requests

import java.time.LocalDate

data class BookingRequest(
    val guestName: String,
    val guestDni: String,
    val guestEmail: String,
    val guestPhone: String? = null,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: String = "PENDING",
    val details: List<BookingDetailRequest>
)