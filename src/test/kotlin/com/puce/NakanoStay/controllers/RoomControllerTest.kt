package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.requests.RoomRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import com.puce.NakanoStay.services.RoomService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
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
import kotlin.test.assertEquals
import com.puce.NakanoStay.models.responses.AvailabilityResponse
import com.puce.NakanoStay.models.responses.DateRange

@WebMvcTest(
    controllers = [RoomController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class RoomControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var roomService: RoomService

    @MockBean
    private lateinit var hotelService: HotelService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    val BASE_URL = Routes.BASE_URL + Routes.ROOM

    @Test
    fun `should return all rooms when get all`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val rooms = listOf(
            Room(hotel, "101", "Single", BigDecimal("50.00"), true),
            Room(hotel, "201", "Double", BigDecimal("80.00"), false)
        )

        `when`(roomService.getAll()).thenReturn(rooms)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].room_number") { value("101") }
                jsonPath("$[0].room_type") { value("Single") }
                jsonPath("$[0].price_per_night") { value(50.00) }
                jsonPath("$[0].is_available") { value(true) }
                jsonPath("$[1].room_number") { value("201") }
                jsonPath("$[1].room_type") { value("Double") }
                jsonPath("$[1].price_per_night") { value(80.00) }
                jsonPath("$[1].is_available") { value(false) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return empty list when no rooms exist`() {
        `when`(roomService.getAll()).thenReturn(emptyList())

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$") { isEmpty() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return rooms by hotel when get by hotel id`() {
        val hotel = Hotel("Beach Resort", "Beach Address", "Coastal City", 5, "beach@resort.com")
        val rooms = listOf(
            Room(hotel, "301", "Suite", BigDecimal("150.00"), true),
            Room(hotel, "302", "Suite", BigDecimal("150.00"), true)
        )

        `when`(roomService.getByHotel(1L)).thenReturn(rooms)

        val result = mockMvc.get("$BASE_URL/hotel/1")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].room_number") { value("301") }
                jsonPath("$[0].room_type") { value("Suite") }
                jsonPath("$[0].price_per_night") { value(150.00) }
                jsonPath("$[1].room_number") { value("302") }
                jsonPath("$[1].room_type") { value("Suite") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return empty list when hotel has no rooms`() {
        `when`(roomService.getByHotel(1L)).thenReturn(emptyList())

        val result = mockMvc.get("$BASE_URL/hotel/1")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isEmpty() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should create room when post`() {
        val hotel = Hotel("Mountain Lodge", "Mountain Road", "Highland", 3, "mountain@lodge.com")
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "A5",
            roomType = "Deluxe",
            pricePerNight = BigDecimal("120.00"),
            isAvailable = true
        )

        val savedRoom = Room(
            hotel = hotel,
            roomNumber = request.roomNumber,
            roomType = request.roomType,
            pricePerNight = request.pricePerNight,
            isAvailable = request.isAvailable
        )

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.save(any())).thenReturn(savedRoom)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.room_number") { value("A5") }
            jsonPath("$.room_type") { value("Deluxe") }
            jsonPath("$.price_per_night") { value(120.00) }
            jsonPath("$.is_available") { value(true) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should create room with null room type when post`() {
        val hotel = Hotel("Simple Hotel", "Simple Address", "Simple City", 3, "simple@hotel.com")
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "B1",
            roomType = null,
            pricePerNight = BigDecimal("60.00"),
            isAvailable = true
        )

        val savedRoom = Room(
            hotel = hotel,
            roomNumber = request.roomNumber,
            roomType = request.roomType,
            pricePerNight = request.pricePerNight,
            isAvailable = request.isAvailable
        )

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.save(any())).thenReturn(savedRoom)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.room_number") { value("B1") }
            jsonPath("$.room_type") { value(null) }
            jsonPath("$.price_per_night") { value(60.00) }
            jsonPath("$.is_available") { value(true) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when hotel not found for room creation`() {
        val request = RoomRequest(
            hotelId = 99L,
            roomNumber = "X1",
            roomType = "Standard",
            pricePerNight = BigDecimal("75.00"),
            isAvailable = true
        )

        `when`(hotelService.getById(99L))
            .thenThrow(NotFoundException("Hotel con id 99 no encontrado"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Hotel con id 99 no encontrado") }
        }
    }

    @Test
    fun `should return 400 when create room with validation error`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "",
            roomType = "Standard",
            pricePerNight = BigDecimal("75.00"),
            isAvailable = true
        )

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.save(any()))
            .thenThrow(ValidationException("El número de habitación es requerido"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("El número de habitación es requerido") }
        }
    }

    @Test
    fun `should return 409 when create room with conflict error`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "101",
            roomType = "Standard",
            pricePerNight = BigDecimal("75.00"),
            isAvailable = true
        )

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.save(any()))
            .thenThrow(ConflictException("Ya existe una habitación con el número '101' en este hotel"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Ya existe una habitación con el número '101' en este hotel") }
        }
    }

    @Test
    fun `should update room when put`() {
        val hotel = Hotel("Updated Hotel", "Updated Address", "Updated City", 4, "updated@hotel.com")
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "102",
            roomType = "Updated",
            pricePerNight = BigDecimal("90.00"),
            isAvailable = false
        )
        val updatedRoom = Room(
            hotel = hotel,
            roomNumber = request.roomNumber,
            roomType = request.roomType,
            pricePerNight = request.pricePerNight,
            isAvailable = request.isAvailable
        ).apply { id = 1L }

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.update(eq(1L), any())).thenReturn(updatedRoom)

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.room_number") { value("102") }
            jsonPath("$.price_per_night") { value(90.00) }
            jsonPath("$.is_available") { value(false) }
        }

        verify(roomService).update(eq(1L), any())
    }

    @Test
    fun `should return 404 when updating non-existent room`() {
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "102",
            roomType = "Updated",
            pricePerNight = BigDecimal("90.00"),
            isAvailable = false
        )

        `when`(hotelService.getById(1L)).thenReturn(Hotel("Updated Hotel", "Updated Address", "Updated City", 4, "updated@hotel.com"))
        `when`(roomService.update(eq(99L), any()))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/99") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
        }
    }

    @Test
    fun `should return 400 when updating room with validation error`() {
        val request = RoomRequest(
            hotelId = 1L,
            roomNumber = "",
            roomType = "Updated",
            pricePerNight = BigDecimal("90.00"),
            isAvailable = false
        )
        val hotel = Hotel("Updated Hotel", "Updated Address", "Updated City", 4, "updated@hotel.com")

        `when`(hotelService.getById(1L)).thenReturn(hotel)
        `when`(roomService.update(eq(1L), any()))
            .thenThrow(ValidationException("El número de habitación es requerido"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("El número de habitación es requerido") }
        }
    }

    @Test
    fun `should make room available when put available`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val availableRoom = Room(hotel, "101", "Single", BigDecimal("50.00"), true).apply { id = 1L }

        `when`(roomService.availableRoom(1L)).thenReturn(availableRoom)

        mockMvc.put("$BASE_URL/1/available")
            .andExpect {
                status { isOk() }
                jsonPath("$.is_available") { value(true) }
            }

        verify(roomService).availableRoom(1L)
    }

    @Test
    fun `should return 404 when making non-existent room available`() {
        `when`(roomService.availableRoom(99L))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        mockMvc.put("$BASE_URL/99/available")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
            }
    }

    @Test
    fun `should return 409 when making already available room available`() {
        `when`(roomService.availableRoom(1L))
            .thenThrow(ConflictException("La habitación ya está disponible"))

        mockMvc.put("$BASE_URL/1/available")
            .andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("La habitación ya está disponible") }
            }
    }

    @Test
    fun `should make room unavailable when put unavailable`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val unavailableRoom = Room(hotel, "101", "Single", BigDecimal("50.00"), false).apply { id = 1L }

        `when`(roomService.unavailableRoom(1L)).thenReturn(unavailableRoom)

        mockMvc.put("$BASE_URL/1/unavailable")
            .andExpect {
                status { isOk() }
                jsonPath("$.is_available") { value(false) }
            }

        verify(roomService).unavailableRoom(1L)
    }

    @Test
    fun `should return 404 when making non-existent room unavailable`() {
        `when`(roomService.unavailableRoom(99L))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        mockMvc.put("$BASE_URL/99/unavailable")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
            }
    }

    @Test
    fun `should return 409 when making already unavailable room unavailable`() {
        `when`(roomService.unavailableRoom(1L))
            .thenThrow(ConflictException("La habitación no está disponible"))

        mockMvc.put("$BASE_URL/1/unavailable")
            .andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("La habitación no está disponible") }
            }
    }

    @Test
    fun `should delete room when delete by id`() {
        val result = mockMvc.delete("$BASE_URL/delete/1")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        verify(roomService).delete(1L)
        assertEquals(204, result.response.status)
    }

    @Test
    fun `should return 404 when deleting non-existent room`() {
        doThrow(NotFoundException("Habitación con id 99 no encontrada"))
            .`when`(roomService).delete(99L)

        mockMvc.delete("$BASE_URL/delete/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
            }
    }

    @Test
    fun `should return 500 when unexpected error occurs`() {
        `when`(roomService.getAll())
            .thenThrow(RuntimeException("Database connection error"))

        mockMvc.get(BASE_URL)
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.error") { value("Unexpected error: Database connection error") }
            }
    }

    @Test
    fun `should return room availability when get availability`() {
        val startDate = LocalDate.of(2025, 6, 1)
        val endDate = LocalDate.of(2025, 6, 10)
        val availability = AvailabilityResponse(
            roomId = 1L,
            availableDates = listOf(
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 2),
                LocalDate.of(2025, 6, 8),
                LocalDate.of(2025, 6, 9)
            ),
            occupiedRanges = listOf(
                DateRange(LocalDate.of(2025, 6, 3), LocalDate.of(2025, 6, 7))
            )
        )

        `when`(roomService.getAvailability(1L, startDate, endDate)).thenReturn(availability)

        val result = mockMvc.get("$BASE_URL/1/availability?startDate=2025-06-01&endDate=2025-06-10")
            .andExpect {
                status { isOk() }
                jsonPath("$.room_id") { value(1) }
                jsonPath("$.available_dates") { isArray() }
                jsonPath("$.available_dates.length()") { value(4) }
                jsonPath("$.available_dates[0]") { value("2025-06-01") }
                jsonPath("$.available_dates[1]") { value("2025-06-02") }
                jsonPath("$.available_dates[2]") { value("2025-06-08") }
                jsonPath("$.available_dates[3]") { value("2025-06-09") }
                jsonPath("$.occupied_ranges") { isArray() }
                jsonPath("$.occupied_ranges.length()") { value(1) }
                jsonPath("$.occupied_ranges[0].start") { value("2025-06-03") }
                jsonPath("$.occupied_ranges[0].end") { value("2025-06-07") }
            }.andReturn()

        assertEquals(200, result.response.status)
        verify(roomService).getAvailability(1L, startDate, endDate)
    }

    @Test
    fun `should return room availability with no occupied ranges when room is always available`() {
        val startDate = LocalDate.of(2025, 6, 1)
        val endDate = LocalDate.of(2025, 6, 5)
        val availability = AvailabilityResponse(
            roomId = 1L,
            availableDates = listOf(
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 2),
                LocalDate.of(2025, 6, 3),
                LocalDate.of(2025, 6, 4)
            ),
            occupiedRanges = emptyList()
        )

        `when`(roomService.getAvailability(1L, startDate, endDate)).thenReturn(availability)

        val result = mockMvc.get("$BASE_URL/1/availability?startDate=2025-06-01&endDate=2025-06-05")
            .andExpect {
                status { isOk() }
                jsonPath("$.room_id") { value(1) }
                jsonPath("$.available_dates.length()") { value(4) }
                jsonPath("$.occupied_ranges") { isEmpty() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return room availability with no available dates when room is fully occupied`() {
        val startDate = LocalDate.of(2025, 6, 1)
        val endDate = LocalDate.of(2025, 6, 5)
        val availability = AvailabilityResponse(
            roomId = 1L,
            availableDates = emptyList(),
            occupiedRanges = listOf(
                DateRange(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 4))
            )
        )

        `when`(roomService.getAvailability(1L, startDate, endDate)).thenReturn(availability)

        val result = mockMvc.get("$BASE_URL/1/availability?startDate=2025-06-01&endDate=2025-06-05")
            .andExpect {
                status { isOk() }
                jsonPath("$.room_id") { value(1) }
                jsonPath("$.available_dates") { isEmpty() }
                jsonPath("$.occupied_ranges.length()") { value(1) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when get availability for non-existent room`() {
        val startDate = LocalDate.of(2025, 6, 1)
        val endDate = LocalDate.of(2025, 6, 5)

        `when`(roomService.getAvailability(99L, startDate, endDate))
            .thenThrow(NotFoundException("Habitación con id 99 no encontrada"))

        mockMvc.get("$BASE_URL/99/availability?startDate=2025-06-01&endDate=2025-06-05")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
            }
    }

    @Test
    fun `should return 400 when get availability with invalid date range`() {
        val startDate = LocalDate.of(2025, 6, 10)
        val endDate = LocalDate.of(2025, 6, 5)

        `when`(roomService.getAvailability(1L, startDate, endDate))
            .thenThrow(ValidationException("La fecha de inicio debe ser anterior a la fecha de fin"))

        mockMvc.get("$BASE_URL/1/availability?startDate=2025-06-10&endDate=2025-06-05")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { value("La fecha de inicio debe ser anterior a la fecha de fin") }
            }
    }

    @Test
    fun `should return 400 when get availability with past start date`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2025, 6, 5)

        `when`(roomService.getAvailability(1L, startDate, endDate))
            .thenThrow(ValidationException("La fecha de inicio no puede ser en el pasado"))

        mockMvc.get("$BASE_URL/1/availability?startDate=2024-01-01&endDate=2025-06-05")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { value("La fecha de inicio no puede ser en el pasado") }
            }
    }
}