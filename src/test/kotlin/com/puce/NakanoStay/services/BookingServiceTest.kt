package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.repositories.BookingDetailRepository
import com.puce.NakanoStay.repositories.BookingRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class BookingServiceTest {
    private lateinit var bookingRepository: BookingRepository
    private lateinit var bookingDetailRepository: BookingDetailRepository
    private lateinit var service: BookingService

    @BeforeEach
    fun setUp() {
        bookingRepository = mock(BookingRepository::class.java)
        bookingDetailRepository = mock(BookingDetailRepository::class.java)
        service = BookingService(bookingRepository, bookingDetailRepository)
    }

    @Test
    fun `should return all bookings`() {
        val user = User("John", "1234567890", "john@puce.edu.ec", "0999999999")
        val bookings = listOf(
            Booking(
                user = user,
                checkIn = LocalDate.of(2025, 1, 15),
                checkOut = LocalDate.of(2025, 1, 20),
                status = "confirmed"
            ),
            Booking(
                user = user,
                checkIn = LocalDate.of(2025, 2, 10),
                checkOut = LocalDate.of(2025, 2, 15),
                status = "pending"
            )
        )

        `when`(bookingRepository.findAll())
            .thenReturn(bookings)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("confirmed", result[0].status)
        assertEquals("pending", result[1].status)
    }

    @Test
    fun `should get a booking by id`() {
        val user = User("Alice", "9876543210", "alice@puce.edu.ec", "0988888888")
        val booking = Booking(
            user = user,
            checkIn = LocalDate.of(2025, 3, 5),
            checkOut = LocalDate.of(2025, 3, 10),
            status = "confirmed"
        )

        `when`(bookingRepository.findById(1L))
            .thenReturn(Optional.of(booking))

        val result = service.getById(1L)

        assertEquals("confirmed", result.status)
        assertEquals(LocalDate.of(2025, 3, 5), result.checkIn)
        assertEquals(LocalDate.of(2025, 3, 10), result.checkOut)
    }

    @Test
    fun `should throw NotFoundException when booking not found by id`() {
        `when`(bookingRepository.findById(99L))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(99L)
        }

        assertEquals("Reserva con id 99 no encontrada", exception.message)
    }

    @Test
    fun `should save a booking`() {
        val user = User("Bob", "1111111111", "bob@puce.edu.ec", "0977777777")
        val booking = Booking(
            user = user,
            checkIn = LocalDate.of(2025, 4, 1),
            checkOut = LocalDate.of(2025, 4, 5),
            status = "pending"
        )

        `when`(bookingRepository.save(booking))
            .thenReturn(booking)

        val savedBooking = service.save(booking)

        assertEquals("pending", savedBooking.status)
        assertEquals(user, savedBooking.user)
    }

    @Test
    fun `should delete a booking by id`() {
        `when`(bookingRepository.existsById(5L))
            .thenReturn(true)

        service.delete(5L)

        verify(bookingRepository).deleteById(5L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent booking`() {
        `when`(bookingRepository.existsById(88L))
            .thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(88L)
        }

        assertEquals("Reserva con id 88 no encontrada", exception.message)
        verify(bookingRepository, never()).deleteById(anyLong())
    }
}