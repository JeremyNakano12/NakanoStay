package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.models.requests.BookingDetailRequest
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.BookingCodeService
import com.puce.NakanoStay.services.BookingService
import com.puce.NakanoStay.services.EmailService
import com.puce.NakanoStay.services.RoomService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.delete
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

@WebMvcTest(
    controllers = [BookingController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class BookingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var bookingService: BookingService

    @MockBean
    private lateinit var roomService: RoomService

    @MockBean
    private lateinit var emailService: EmailService

    @MockBean
    private lateinit var bookingCodeService: BookingCodeService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        doNothing().`when`(emailService).sendBookingConfirmation(any())
        doNothing().`when`(emailService).sendCancellationNotification(any())
        doNothing().`when`(emailService).sendBookingLastConfirmation(any())
        doNothing().`when`(emailService).sendBookingCompletedNotification(any())
    }

    val BASE_URL = Routes.BASE_URL + Routes.BOOKINGS

    @Test
    fun `should return all bookings when get all`() {
        val bookings = listOf(
            createTestBooking("NKS-ABC123250814", "John Doe", "1234567890", "john@test.com", BookingStatus.CONFIRMED),
            createTestBooking("NKS-DEF456250814", "Jane Smith", "0987654321", "jane@test.com", BookingStatus.PENDING)
        )

        `when`(bookingService.getAll()).thenReturn(bookings)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].booking_code") { value("NKS-ABC123250814") }
                jsonPath("$[0].guest_name") { value("John Doe") }
                jsonPath("$[0].status") { value("CONFIRMED") }
                jsonPath("$[1].booking_code") { value("NKS-DEF456250814") }
                jsonPath("$[1].guest_name") { value("Jane Smith") }
                jsonPath("$[1].status") { value("PENDING") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return empty list when no bookings exist`() {
        `when`(bookingService.getAll()).thenReturn(emptyList())

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$") { isEmpty() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return booking when get by id`() {
        val booking = createTestBooking("NKS-XYZ789250814", "Alice Brown", "1122334455", "alice@test.com", BookingStatus.CONFIRMED)

        `when`(bookingService.getById(1L)).thenReturn(booking)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.booking_code") { value("NKS-XYZ789250814") }
                jsonPath("$.guest_name") { value("Alice Brown") }
                jsonPath("$.guest_dni") { value("1122334455") }
                jsonPath("$.status") { value("CONFIRMED") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return booking when get by code and dni`() {
        val booking = createTestBooking("NKS-CODE123456", "Test User", "1234567890", "test@test.com", BookingStatus.PENDING)

        `when`(bookingService.getByCodeAndDni("NKS-CODE123456", "1234567890")).thenReturn(booking)

        val result = mockMvc.get("$BASE_URL/code/NKS-CODE123456?dni=1234567890")
            .andExpect {
                status { isOk() }
                jsonPath("$.booking_code") { value("NKS-CODE123456") }
                jsonPath("$.guest_name") { value("Test User") }
                jsonPath("$.guest_dni") { value("1234567890") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when get booking by non-existent id`() {
        val bookingId = 88L

        `when`(bookingService.getById(bookingId))
            .thenThrow(NotFoundException("Reserva con id $bookingId no encontrada"))

        val result = mockMvc.get("$BASE_URL/88")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva con id 88 no encontrada") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun `should return 404 when get booking by non-existent code and dni`() {
        `when`(bookingService.getByCodeAndDni("INVALID-CODE", "1234567890"))
            .thenThrow(NotFoundException("Reserva no encontrada o datos incorrectos"))

        mockMvc.get("$BASE_URL/code/INVALID-CODE?dni=1234567890")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva no encontrada o datos incorrectos") }
            }
    }

    @Test
    fun `should create booking when post`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val detailRequest = BookingDetailRequest(roomId = 1L, guests = 2)
        val request = BookingRequest(
            guestName = "Bob Johnson",
            guestDni = "1234567890",
            guestEmail = "bob@test.com",
            guestPhone = "0977777777",
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "PENDING",
            details = listOf(detailRequest)
        )

        val booking = createTestBooking("NKS-GENERATED123", request.guestName, request.guestDni, request.guestEmail, BookingStatus.PENDING)

        `when`(bookingCodeService.generateUniqueBookingCode()).thenReturn("NKS-GENERATED123")
        `when`(roomService.getById(1L)).thenReturn(room)
        `when`(bookingService.save(any())).thenReturn(booking)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.booking_code") { value("NKS-GENERATED123") }
            jsonPath("$.guest_name") { value("Bob Johnson") }
            jsonPath("$.status") { value("PENDING") }
        }.andReturn()

        assertEquals(200, result.response.status)
        verify(emailService).sendBookingConfirmation(any())
    }

    @Test
    fun `should create booking with null phone when post`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val detailRequest = BookingDetailRequest(roomId = 1L, guests = 2)
        val request = BookingRequest(
            guestName = "Charlie Brown",
            guestDni = "1111111111",
            guestEmail = "charlie@test.com",
            guestPhone = null,
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "PENDING",
            details = listOf(detailRequest)
        )

        val booking = createTestBookingWithPhone("NKS-GENERATED456", request.guestName, request.guestDni, request.guestEmail, BookingStatus.PENDING, null)

        `when`(bookingCodeService.generateUniqueBookingCode()).thenReturn("NKS-GENERATED456")
        `when`(roomService.getById(1L)).thenReturn(room)
        `when`(bookingService.save(any())).thenReturn(booking)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.booking_code") { value("NKS-GENERATED456") }
            jsonPath("$.guest_name") { value("Charlie Brown") }
            jsonPath("$.guest_phone") { value(null) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when room not found for booking creation`() {
        val detailRequest = BookingDetailRequest(roomId = 99L, guests = 2)
        val request = BookingRequest(
            guestName = "Charlie Brown",
            guestDni = "2222222222",
            guestEmail = "charlie@test.com",
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "PENDING",
            details = listOf(detailRequest)
        )

        `when`(bookingCodeService.generateUniqueBookingCode()).thenReturn("NKS-TEST123456")
        `when`(roomService.getById(99L))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
        }
    }

    @Test
    fun `should return 400 when create booking with validation error`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val detailRequest = BookingDetailRequest(roomId = 1L, guests = 2)
        val request = BookingRequest(
            guestName = "",
            guestDni = "1234567890",
            guestEmail = "invalid@test.com",
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "PENDING",
            details = listOf(detailRequest)
        )

        `when`(bookingCodeService.generateUniqueBookingCode()).thenReturn("NKS-TEST123456")
        `when`(roomService.getById(1L)).thenReturn(room)
        `when`(bookingService.save(any()))
            .thenThrow(ValidationException("El nombre del huésped es requerido"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("El nombre del huésped es requerido") }
        }
    }

    @Test
    fun `should return 409 when create booking with conflict error`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        val detailRequest = BookingDetailRequest(roomId = 1L, guests = 2)
        val request = BookingRequest(
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "PENDING",
            details = listOf(detailRequest)
        )

        `when`(bookingCodeService.generateUniqueBookingCode()).thenReturn("NKS-TEST123456")
        `when`(roomService.getById(1L)).thenReturn(room)
        `when`(bookingService.save(any()))
            .thenThrow(ConflictException("La habitación 101 no está disponible"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("La habitación 101 no está disponible") }
        }
    }

    @Test
    fun `should cancel booking successfully`() {
        val booking = createTestBooking("NKS-CANCEL123", "Test User", "1234567890", "test@test.com", BookingStatus.CANCELLED)

        `when`(bookingService.cancelBooking("NKS-CANCEL123", "1234567890")).thenReturn(booking)

        val result = mockMvc.put("$BASE_URL/code/NKS-CANCEL123/cancel?dni=1234567890")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("CANCELLED") }
            }.andReturn()

        assertEquals(200, result.response.status)
        verify(emailService).sendCancellationNotification(any())
    }

    @Test
    fun `should return 409 when trying to cancel already cancelled booking`() {
        `when`(bookingService.cancelBooking("NKS-CANCEL123", "1234567890"))
            .thenThrow(ConflictException("La reserva ya está cancelada"))

        mockMvc.put("$BASE_URL/code/NKS-CANCEL123/cancel?dni=1234567890")
            .andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("La reserva ya está cancelada") }
            }
    }

    @Test
    fun `should return 404 when cancel non-existent booking`() {
        `when`(bookingService.cancelBooking("INVALID-CODE", "1234567890"))
            .thenThrow(NotFoundException("Reserva no encontrada o datos incorrectos"))

        mockMvc.put("$BASE_URL/code/INVALID-CODE/cancel?dni=1234567890")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva no encontrada o datos incorrectos") }
            }
    }

    @Test
    fun `should confirm booking successfully`() {
        val booking = createTestBooking("NKS-CONFIRM123", "Test User", "1234567890", "test@test.com", BookingStatus.CONFIRMED)

        `when`(bookingService.confirmBooking("NKS-CONFIRM123", "1234567890")).thenReturn(booking)

        val result = mockMvc.put("$BASE_URL/code/NKS-CONFIRM123/confirm?dni=1234567890")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("CONFIRMED") }
            }.andReturn()

        assertEquals(200, result.response.status)
        verify(emailService).sendBookingLastConfirmation(any())
    }

    @Test
    fun `should return 409 when trying to confirm already confirmed booking`() {
        `when`(bookingService.confirmBooking("NKS-CONFIRM123", "1234567890"))
            .thenThrow(ConflictException("La reserva ya está confirmada"))

        mockMvc.put("$BASE_URL/code/NKS-CONFIRM123/confirm?dni=1234567890")
            .andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("La reserva ya está confirmada") }
            }
    }

    @Test
    fun `should return 404 when confirm non-existent booking`() {
        `when`(bookingService.confirmBooking("INVALID-CODE", "1234567890"))
            .thenThrow(NotFoundException("Reserva no encontrada o datos incorrectos"))

        mockMvc.put("$BASE_URL/code/INVALID-CODE/confirm?dni=1234567890")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva no encontrada o datos incorrectos") }
            }
    }

    @Test
    fun `should complete booking successfully`() {
        val booking = createTestBooking("NKS-COMPLETE123", "Test User", "1234567890", "test@test.com", BookingStatus.COMPLETED)

        `when`(bookingService.completeBooking("NKS-COMPLETE123", "1234567890")).thenReturn(booking)

        val result = mockMvc.put("$BASE_URL/code/NKS-COMPLETE123/complete?dni=1234567890")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("COMPLETED") }
            }.andReturn()

        assertEquals(200, result.response.status)
        verify(emailService).sendBookingCompletedNotification(any())
    }

    @Test
    fun `should return 409 when trying to complete non-confirmed booking`() {
        `when`(bookingService.completeBooking("NKS-COMPLETE123", "1234567890"))
            .thenThrow(ConflictException("No se puede completar una reserva no confirmada"))

        mockMvc.put("$BASE_URL/code/NKS-COMPLETE123/complete?dni=1234567890")
            .andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("No se puede completar una reserva no confirmada") }
            }
    }

    @Test
    fun `should return 404 when complete non-existent booking`() {
        `when`(bookingService.completeBooking("INVALID-CODE", "1234567890"))
            .thenThrow(NotFoundException("Reserva no encontrada o datos incorrectos"))

        mockMvc.put("$BASE_URL/code/INVALID-CODE/complete?dni=1234567890")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva no encontrada o datos incorrectos") }
            }
    }

    @Test
    fun `should delete booking when delete by id`() {
        val result = mockMvc.delete("$BASE_URL/delete/10")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        verify(bookingService).delete(10L)
        assertEquals(204, result.response.status)
    }

    @Test
    fun `should return 404 when deleting non-existent booking`() {
        doThrow(NotFoundException("Reserva con id 99 no encontrada"))
            .`when`(bookingService).delete(99L)

        mockMvc.delete("$BASE_URL/delete/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Reserva con id 99 no encontrada") }
            }
    }

    @Test
    fun `should return 500 when unexpected error occurs`() {
        `when`(bookingService.getAll())
            .thenThrow(RuntimeException("Database connection error"))

        mockMvc.get(BASE_URL)
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.error") { value("Unexpected error: Database connection error") }
            }
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

    private fun createTestBookingWithPhone(
        bookingCode: String,
        guestName: String,
        guestDni: String,
        guestEmail: String,
        status: BookingStatus,
        guestPhone: String?
    ): Booking {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        val booking = Booking(
            bookingCode = bookingCode,
            guestName = guestName,
            guestDni = guestDni,
            guestEmail = guestEmail,
            guestPhone = guestPhone,
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