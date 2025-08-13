package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.enums.BookingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.*

interface BookingRepository : JpaRepository<Booking, Long> {
    fun existsByBookingCode(bookingCode: String): Boolean
    fun findByBookingCodeAndGuestDni(bookingCode: String, guestDni: String): Optional<Booking>

    @Query("""
        SELECT COUNT(b) > 0 
        FROM Booking b 
        JOIN b.bookingDetails bd 
        WHERE bd.room.id IN :roomIds 
        AND b.status NOT IN :excludedStatuses
        AND (
            (b.checkIn <= :checkOut AND b.checkOut > :checkIn)
        )
    """)
    fun existsConflictingBooking(
        @Param("roomIds") roomIds: List<Long>,
        @Param("checkIn") checkIn: LocalDate,
        @Param("checkOut") checkOut: LocalDate,
        @Param("excludedStatuses") excludedStatuses: List<BookingStatus> = listOf(BookingStatus.CANCELLED)
    ): Boolean
}