package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.requests.RoomRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import com.puce.NakanoStay.services.RoomService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
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
import kotlin.test.assertEquals

@WebMvcTest(RoomController::class)
@Import(RoomMockConfig::class)
class RoomControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
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
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4)
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
                jsonPath("$[1].isAvailable") { value(false) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return rooms by hotel when get by hotel id`() {
        val hotel = Hotel("Beach Resort", "Beach Address", "Coastal City", 5)
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
    fun `should create room when post`() {
        val hotel = Hotel("Mountain Lodge", "Mountain Road", "Highland", 3)
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
        }
    }

    @Test
    fun `should delete room when delete by id`() {
        val result = mockMvc.delete("$BASE_URL/delete/7")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        verify(roomService).delete(7L)
        assertEquals(204, result.response.status)
    }
}

@TestConfiguration
class RoomMockConfig {
    @Bean
    fun roomService(): RoomService = mock(RoomService::class.java)

    @Bean
    fun hotelService(): HotelService = mock(HotelService::class.java)
}