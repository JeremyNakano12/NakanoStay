package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Booking
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookingRepository : JpaRepository<Booking, Long> {
    fun existsByBookingCode(bookingCode: String): Boolean
    fun findByBookingCodeAndGuestDni(bookingCode: String, guestDni: String): Optional<Booking>
}