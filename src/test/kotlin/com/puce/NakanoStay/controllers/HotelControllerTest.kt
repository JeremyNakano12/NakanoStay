package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.requests.HotelRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import kotlin.test.assertEquals

@WebMvcTest(HotelController::class)
@Import(HotelMockConfig::class)
class HotelControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var hotelService: HotelService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    val BASE_URL = Routes.BASE_URL + Routes.HOTELS

    @Test
    fun `should return all hotels when get all`() {
        val hotels = listOf(
            Hotel("Grand Palace", "123 Main St", "Quito", 5),
            Hotel("Budget Inn", "456 Side St", "Guayaquil", 3)
        )

        `when`(hotelService.getAll()).thenReturn(hotels)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("Grand Palace") }
                jsonPath("$[0].city") { value("Quito") }
                jsonPath("$[0].stars") { value(5) }
                jsonPath("$[1].name") { value("Budget Inn") }
                jsonPath("$[1].city") { value("Guayaquil") }
                jsonPath("$[1].stars") { value(3) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return hotel when get by id`() {
        val hotel = Hotel("Luxury Resort", "789 Beach Ave", "Manta", 4)

        `when`(hotelService.getById(1L)).thenReturn(hotel)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Luxury Resort") }
                jsonPath("$.address") { value("789 Beach Ave") }
                jsonPath("$.city") { value("Manta") }
                jsonPath("$.stars") { value(4) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when get hotel by non-existent id`() {
        val hotelId = 77L

        `when`(hotelService.getById(hotelId))
            .thenThrow(NotFoundException("Hotel con id $hotelId no encontrado"))

        val result = mockMvc.get("$BASE_URL/77")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun `should create hotel when post`() {
        val request = HotelRequest(
            name = "New Hotel",
            address = "999 New Street",
            city = "Cuenca",
            stars = 4
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars
        )

        `when`(hotelService.save(hotel)).thenReturn(hotel)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("New Hotel") }
            jsonPath("$.address") { value("999 New Street") }
            jsonPath("$.city") { value("Cuenca") }
            jsonPath("$.stars") { value(4) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should delete hotel when delete by id`() {
        val result = mockMvc.delete("$BASE_URL/delete/3")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        verify(hotelService).delete(3L)
        assertEquals(204, result.response.status)
    }
}

@TestConfiguration
class HotelMockConfig {
    @Bean
    fun hotelService(): HotelService = mock(HotelService::class.java)
}