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
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.delete
import java.math.BigDecimal
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class, SpringExtension::class)
class RoomControllerTest {

    @Mock
    private lateinit var roomService: RoomService

    @Mock
    private lateinit var hotelService: HotelService

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        val controller = RoomController(roomService, hotelService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

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
                jsonPath("$[0].roomNumber") { value("101") }
                jsonPath("$[0].roomType") { value("Single") }
                jsonPath("$[0].pricePerNight") { value(50.00) }
                jsonPath("$[0].isAvailable") { value(true) }
                jsonPath("$[1].roomNumber") { value("201") }
                jsonPath("$[1].roomType") { value("Double") }
                jsonPath("$[1].pricePerNight") { value(80.00) }
                jsonPath("$[1].isAvailable") { value(false) }
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
                jsonPath("$[0].roomNumber") { value("301") }
                jsonPath("$[0].roomType") { value("Suite") }
                jsonPath("$[0].pricePerNight") { value(150.00) }
                jsonPath("$[1].roomNumber") { value("302") }
                jsonPath("$[1].roomType") { value("Suite") }
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
            jsonPath("$.roomNumber") { value("A5") }
            jsonPath("$.roomType") { value("Deluxe") }
            jsonPath("$.pricePerNight") { value(120.00) }
            jsonPath("$.isAvailable") { value(true) }
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
            jsonPath("$.roomNumber") { value("B1") }
            jsonPath("$.roomType") { value(null) }
            jsonPath("$.pricePerNight") { value(60.00) }
            jsonPath("$.isAvailable") { value(true) }
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
            jsonPath("$.roomNumber") { value("102") }
            jsonPath("$.pricePerNight") { value(90.00) }
            jsonPath("$.isAvailable") { value(false) }
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
    fun `should delete room when delete by id`() {
        val roomId = 1L
        doNothing().`when`(roomService).delete(roomId)

        mockMvc.delete("$BASE_URL/$roomId")
            .andExpect {
                status { isOk() }
            }
        verify(roomService).delete(roomId)
    }

    @Test
    fun `should return 404 when deleting non-existent room`() {
        val roomId = 99L
        doThrow(NotFoundException("Habitación con id $roomId no encontrada")).`when`(roomService).delete(roomId)

        mockMvc.delete("$BASE_URL/$roomId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Habitación con id 99 no encontrada") }
            }
    }
}