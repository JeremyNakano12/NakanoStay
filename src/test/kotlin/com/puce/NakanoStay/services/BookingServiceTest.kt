package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.repositories.BookingRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class BookingServiceTest {
    private lateinit var bookingRepository: BookingRepository
    private lateinit var service: BookingService

    @BeforeEach
    fun setUp() {
        bookingRepository = mock(BookingRepository::class.java)
        service = BookingService(bookingRepository)
    }

    @Test
    fun `should save a valid booking`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            guestPhone = "0999999999",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        `when`(bookingRepository.existsConflictingBooking(
            listOf(1L),
            booking.checkIn,
            booking.checkOut,
            listOf(BookingStatus.CANCELLED)
        )).thenReturn(false)
        `when`(bookingRepository.save(booking)).thenReturn(booking)

        val savedBooking = service.save(booking)

        assertEquals("John Doe", savedBooking.guestName)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should throw ValidationException when guest name is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El nombre del huésped es requerido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when DNI is invalid`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "123456789", // Solo 9 dígitos
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El DNI debe tener exactamente 10 dígitos", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when email is invalid`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "invalid-email",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El formato del email es inválido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when check-in is in the past`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().minusDays(1), // Ayer
            checkOut = LocalDate.now().plusDays(1),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La fecha de check-in no puede ser en el pasado", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when check-out is before check-in`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(3),
            checkOut = LocalDate.now().plusDays(1), // Antes del check-in
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La fecha de check-out debe ser posterior a la fecha de check-in", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when stay is longer than 30 days`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(32), // 31 días
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La estadía no puede ser mayor a 30 días", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when room is not available`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), false).apply { id = 1L } // No disponible

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        `when`(bookingRepository.existsConflictingBooking(
            listOf(1L),
            booking.checkIn,
            booking.checkOut,
            listOf(BookingStatus.CANCELLED)
        )).thenReturn(false)

        val exception = assertThrows<ConflictException> {
            service.save(booking)
        }

        assertEquals("La habitación 101 no está disponible", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when there are conflicting bookings`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("50.00"))
        booking.bookingDetails.add(bookingDetail)

        `when`(bookingRepository.existsConflictingBooking(
            listOf(1L),
            booking.checkIn,
            booking.checkOut,
            listOf(BookingStatus.CANCELLED)
        )).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.save(booking)
        }

        assertTrue(exception.message!!.contains("Una o más habitaciones no están disponibles para las fechas seleccionadas"))
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guests number is invalid`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 0, BigDecimal("50.00")) // 0 huéspedes
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El número de huéspedes debe ser mayor a 0", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should cancel booking successfully`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val existingBooking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.CONFIRMED
        ).apply { id = 1L }

        val bookingDetail = BookingDetail(existingBooking, room, 2, BigDecimal("50.00"))
        existingBooking.bookingDetails.add(bookingDetail)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))
        `when`(bookingRepository.save(any<Booking>())).thenAnswer { it.arguments[0] }

        val cancelledBooking = service.cancelBooking("NKS-ABC123250814", "1234567890")

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.status)
        verify(bookingRepository).save(any<Booking>())
    }

    @Test
    fun `should throw ConflictException when trying to cancel already cancelled booking`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val existingBooking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.CANCELLED // Ya cancelada
        ).apply { id = 1L }

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.cancelBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("La reserva ya está cancelada", exception.message)
        verify(bookingRepository, never()).save(any())
    }
}