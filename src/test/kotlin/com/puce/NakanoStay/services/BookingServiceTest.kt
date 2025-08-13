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
import java.time.LocalDateTime
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
    fun `should return all bookings`() {
        val bookings = listOf(
            createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CONFIRMED),
            createTestBooking("NKS-DEF456250814", "Jane Smith", "0987654321", "jane@test.com", BookingStatus.PENDING)
        )

        `when`(bookingRepository.findAll()).thenReturn(bookings)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("John Doe", result[0].guestName)
        assertEquals("Jane Smith", result[1].guestName)
    }

    @Test
    fun `should return empty list when no bookings exist`() {
        `when`(bookingRepository.findAll()).thenReturn(emptyList())

        val result = service.getAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return booking by id`() {
        val booking = createTestBooking("NKS-TEST123456", "Test User", "1234567890", "test@test.com", BookingStatus.PENDING)

        `when`(bookingRepository.findById(1L)).thenReturn(Optional.of(booking))

        val result = service.getById(1L)

        assertEquals("Test User", result.guestName)
        assertEquals("NKS-TEST123456", result.bookingCode)
    }

    @Test
    fun `should throw NotFoundException when booking not found by id`() {
        `when`(bookingRepository.findById(99L)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(99L)
        }

        assertEquals("Reserva con id 99 no encontrada", exception.message)
    }

    @Test
    fun `should return booking by code and dni`() {
        val booking = createTestBooking("NKS-SEARCH123", "Search User", "1234567890", "search@test.com", BookingStatus.CONFIRMED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-SEARCH123", "1234567890"))
            .thenReturn(Optional.of(booking))

        val result = service.getByCodeAndDni("NKS-SEARCH123", "1234567890")

        assertEquals("Search User", result.guestName)
        assertEquals("NKS-SEARCH123", result.bookingCode)
    }

    @Test
    fun `should throw NotFoundException when booking not found by code and dni`() {
        `when`(bookingRepository.findByBookingCodeAndGuestDni("INVALID", "2222222222"))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getByCodeAndDni("INVALID", "2222222222")
        }

        assertEquals("Reserva no encontrada o datos incorrectos", exception.message)
    }

    @Test
    fun `should save a valid booking`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "0999999999",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
    fun `should save booking with null phone`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = null,
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
        assertNull(savedBooking.guestPhone)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should save booking with empty phone`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
        assertEquals("", savedBooking.guestPhone)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should save booking with valid international phone`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "+1-555-123-4567",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
        assertEquals("+1-555-123-4567", savedBooking.guestPhone)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should save booking for multiple guests`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "0999999999",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 10, BigDecimal("100.00")) // 10 huéspedes (máximo)
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
        assertEquals(10, savedBooking.bookingDetails.first().guests)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should throw ValidationException when booking code is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El código de reserva es requerido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when booking code is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "A".repeat(17), // 17 caracteres
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El código de reserva no puede tener más de 16 caracteres", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guest name is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El nombre del huésped es requerido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guest name is too short`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "A", // 1 carácter
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El nombre del huésped debe tener al menos 2 caracteres", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guest name is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "A".repeat(101), // 101 caracteres
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El nombre del huésped no puede tener más de 100 caracteres", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when DNI is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El DNI del huésped es requerido", exception.message)
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

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La cédula debe ser valida", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should validate ecuadorian DNI correctly`() {
        val validDnis = listOf(
            "2222222222",
        )

        val invalidDnis = listOf(
            "123456789",
            "12345678901",
            "abc1234567",
            "9999999999",
            "2612345678"
        )

        validDnis.forEach { dni ->
            assertTrue(service.validateDni(dni), "DNI $dni should be valid")
        }

        invalidDnis.forEach { dni ->
            assertFalse(service.validateDni(dni), "DNI $dni should be invalid")
        }
    }

    @Test
    fun `should throw ValidationException when email is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El email del huésped es requerido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when email is invalid`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "invalid-email",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El formato del email es inválido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when email is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val longEmail = "a".repeat(92) + "@test.com"
        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = longEmail,
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El email no puede tener más de 100 caracteres", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when phone is too short`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "123456", // Muy corto
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El teléfono debe ser valido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when phone is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "1234567890123456", // Muy largo
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El teléfono debe ser valido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when phone has invalid characters`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "123-abc-4567", // Contiene letras
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El teléfono debe ser valido", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when phone has too few digits`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            guestPhone = "+1-234-567", // Solo 7 dígitos
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El teléfono debe contener al menos 9 dígitos", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when booking has no details`() {
        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La reserva debe tener al menos una habitación", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guests number is zero`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 0, BigDecimal("100.00")) // 0 huéspedes
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El número de huéspedes debe ser mayor a 0", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when guests number is too high`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 11, BigDecimal("100.00")) // 11 huéspedes
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("El número de huéspedes no puede ser mayor a 10 por habitación", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when check-in is in the past`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().minusDays(1), // Ayer
            checkOut = LocalDate.now().plusDays(1),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(3),
            checkOut = LocalDate.now().plusDays(1), // Antes del check-in
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<ValidationException> {
            service.save(booking)
        }

        assertEquals("La fecha de check-out debe ser posterior a la fecha de check-in", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when check-out equals check-in`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val checkDate = LocalDate.now().plusDays(1)
        val booking = Booking(
            bookingCode = "NKS-ABC123250814",
            guestName = "John Doe",
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = checkDate,
            checkOut = checkDate,
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(32), // 32 días
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
            guestDni = "2222222222",
            guestEmail = "john@test.com",
            checkIn = LocalDate.now().plusDays(1),
            checkOut = LocalDate.now().plusDays(3),
            status = BookingStatus.PENDING
        )

        val bookingDetail = BookingDetail(booking, room, 2, BigDecimal("100.00"))
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
    fun `should cancel booking successfully`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CONFIRMED)
        existingBooking.id = 1L

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))
        `when`(bookingRepository.save(any<Booking>())).thenAnswer { it.arguments[0] }

        val cancelledBooking = service.cancelBooking("NKS-ABC123250814", "1234567890")

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.status)
        verify(bookingRepository).save(any<Booking>())
    }

    @Test
    fun `should throw ConflictException when trying to cancel already cancelled booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CANCELLED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.cancelBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("La reserva ya está cancelada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when trying to cancel completed booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.COMPLETED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.cancelBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("No se puede cancelar una reserva completada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should confirm booking successfully`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.PENDING)
        existingBooking.id = 1L

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))
        `when`(bookingRepository.save(any<Booking>())).thenAnswer { it.arguments[0] }

        val confirmedBooking = service.confirmBooking("NKS-ABC123250814", "1234567890")

        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.status)
        verify(bookingRepository).save(any<Booking>())
    }

    @Test
    fun `should throw ConflictException when trying to confirm already confirmed booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CONFIRMED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.confirmBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("La reserva ya está confirmada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when trying to confirm cancelled booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CANCELLED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.confirmBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("No se puede confirmar una reserva cancelada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when trying to confirm completed booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.COMPLETED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.confirmBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("No se puede confirmar una reserva completada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should complete booking successfully`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CONFIRMED)
        existingBooking.id = 1L

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))
        `when`(bookingRepository.save(any<Booking>())).thenAnswer { it.arguments[0] }

        val completedBooking = service.completeBooking("NKS-ABC123250814", "1234567890")

        assertEquals(BookingStatus.COMPLETED, completedBooking.status)
        verify(bookingRepository).save(any<Booking>())
    }

    @Test
    fun `should throw ConflictException when trying to complete already completed booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.COMPLETED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.completeBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("La reserva ya está completada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when trying to complete pending booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.PENDING)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.completeBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("No se puede completar una reserva no confirmada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when trying to complete cancelled booking`() {
        val existingBooking = createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CANCELLED)

        `when`(bookingRepository.findByBookingCodeAndGuestDni("NKS-ABC123250814", "1234567890"))
            .thenReturn(Optional.of(existingBooking))

        val exception = assertThrows<ConflictException> {
            service.completeBooking("NKS-ABC123250814", "1234567890")
        }

        assertEquals("No se puede completar una reserva cancelada", exception.message)
        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should delete booking successfully`() {
        `when`(bookingRepository.existsById(1L)).thenReturn(true)

        service.delete(1L)

        verify(bookingRepository).deleteById(1L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent booking`() {
        `when`(bookingRepository.existsById(99L)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(99L)
        }

        assertEquals("Reserva con id 99 no encontrada", exception.message)
        verify(bookingRepository, never()).deleteById(any())
    }

    private fun createTestBooking(
        bookingCode: String,
        guestName: String,
        guestDni: String,
        guestEmail: String,
        status: BookingStatus
    ): Booking {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        val booking = Booking(
            bookingCode = bookingCode,
            guestName = guestName,
            guestDni = guestDni,
            guestEmail = guestEmail,
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 3, 15),
            checkOut = LocalDate.of(2025, 3, 20),
            status = status
        )

        val bookingDetail = BookingDetail(
            booking = booking,
            room = room,
            guests = 2,
            priceAtBooking = BigDecimal("250.00")
        )

        booking.bookingDetails.add(bookingDetail)
        return booking
    }
}