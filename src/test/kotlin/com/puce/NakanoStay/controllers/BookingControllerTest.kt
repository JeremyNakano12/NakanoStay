package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.models.requests.BookingDetailRequest
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.BookingService
import com.puce.NakanoStay.services.RoomService
import com.puce.NakanoStay.services.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.delete
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

@WebMvcTest(BookingController::class)
@Import(BookingMockConfig::class)
class BookingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookingService: BookingService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var roomService: RoomService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    val BASE_URL = Routes.BASE_URL + Routes.BOOKINGS

    @Test
    fun `should return all bookings when get all`() {
        val user = User("John Doe", "1234567890", "john@puce.edu.ec", "0999999999")
        val bookings = listOf(
            Booking(
                user = user,
                checkIn = LocalDate.of(2025, 3, 15),
                checkOut = LocalDate.of(2025, 3, 20),
                status = "confirmed"
            ),
            Booking(
                user = user,
                checkIn = LocalDate.of(2025, 4, 10),
                checkOut = LocalDate.of(2025, 4, 15),
                status = "pending"
            )
        )

        `when`(bookingService.getAll()).thenReturn(bookings)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].status") { value("confirmed") }
                jsonPath("$[0].checkIn") { value("2025-03-15") }
                jsonPath("$[0].checkOut") { value("2025-03-20") }
                jsonPath("$[1].status") { value("pending") }
                jsonPath("$[1].checkIn") { value("2025-04-10") }
                jsonPath("$[1].checkOut") { value("2025-04-15") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return booking when get by id`() {
        val user = User("Alice Smith", "9876543210", "alice@puce.edu.ec", "0988888888")
        val booking = Booking(
            user = user,
            checkIn = LocalDate.of(2025, 5, 1),
            checkOut = LocalDate.of(2025, 5, 7),
            status = "confirmed"
        )

        `when`(bookingService.getById(1L)).thenReturn(booking)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("confirmed") }
                jsonPath("$.checkIn") { value("2025-05-01") }
                jsonPath("$.checkOut") { value("2025-05-07") }
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
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun `should create booking when post`() {
        val user = User("Bob Johnson", "1111111111", "bob@puce.edu.ec", "0977777777")
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4)
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        val detailRequest = BookingDetailRequest(
            roomId = 1L,
            guests = 2,
            priceAtBooking = BigDecimal("50.00")
        )

        val request = BookingRequest(
            userId = 1L,
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "pending",
            details = listOf(detailRequest)
        )

        val booking = Booking(
            user = user,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            status = request.status
        )

        `when`(userService.getById(1L)).thenReturn(user)
        `when`(roomService.getById(1L)).thenReturn(room)
        `when`(bookingService.save(any())).thenReturn(booking)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("pending") }
            jsonPath("$.checkIn") { value("2025-06-01") }
            jsonPath("$.checkOut") { value("2025-06-05") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when user not found for booking creation`() {
        val detailRequest = BookingDetailRequest(
            roomId = 1L,
            guests = 2,
            priceAtBooking = BigDecimal("50.00")
        )

        val request = BookingRequest(
            userId = 99L,
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "pending",
            details = listOf(detailRequest)
        )

        `when`(userService.getById(99L))
            .thenThrow(NotFoundException("Usuario con id 99 no encontrado"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return 404 when room not found for booking creation`() {
        val user = User("Charlie Brown", "2222222222", "charlie@puce.edu.ec", "0966666666")

        val detailRequest = BookingDetailRequest(
            roomId = 99L,
            guests = 2,
            priceAtBooking = BigDecimal("50.00")
        )

        val request = BookingRequest(
            userId = 1L,
            checkIn = LocalDate.of(2025, 6, 1),
            checkOut = LocalDate.of(2025, 6, 5),
            status = "pending",
            details = listOf(detailRequest)
        )

        val booking = Booking(
            user = user,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            status = request.status
        )

        `when`(userService.getById(1L)).thenReturn(user)
        `when`(bookingService.save(any())).thenReturn(booking)
        `when`(roomService.getById(99L))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
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
}

@TestConfiguration
class BookingMockConfig {
    @Bean
    fun bookingService(): BookingService = mock(BookingService::class.java)

    @Bean
    fun userService(): UserService = mock(UserService::class.java)

    @Bean
    fun roomService(): RoomService = mock(RoomService::class.java)
}