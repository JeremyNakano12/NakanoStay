package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.requests.HotelRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.Mockito.mock
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
import kotlin.test.assertEquals

@WebMvcTest(
    controllers = [HotelController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class HotelControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
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
            Hotel("Grand Palace", "123 Main St", "Quito", 5, "grand@palace.com"),
            Hotel("Budget Inn", "456 Side St", "Guayaquil", 3, "budget@inn.com")
        )

        `when`(hotelService.getAll()).thenReturn(hotels)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("Grand Palace") }
                jsonPath("$[0].city") { value("Quito") }
                jsonPath("$[0].stars") { value(5) }
                jsonPath("$[0].email") { value("grand@palace.com") }
                jsonPath("$[1].name") { value("Budget Inn") }
                jsonPath("$[1].city") { value("Guayaquil") }
                jsonPath("$[1].stars") { value(3) }
                jsonPath("$[1].email") { value("budget@inn.com") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return empty list when no hotels exist`() {
        `when`(hotelService.getAll()).thenReturn(emptyList())

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$") { isEmpty() }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return hotel when get by id`() {
        val hotel = Hotel("Luxury Resort", "789 Beach Ave", "Manta", 4, "luxury@resort.com")

        `when`(hotelService.getById(1L)).thenReturn(hotel)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Luxury Resort") }
                jsonPath("$.address") { value("789 Beach Ave") }
                jsonPath("$.city") { value("Manta") }
                jsonPath("$.stars") { value(4) }
                jsonPath("$.email") { value("luxury@resort.com") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return hotel with null values when get by id`() {
        val hotel = Hotel("Simple Hotel", "Simple Address", null, null, "simple@hotel.com")

        `when`(hotelService.getById(1L)).thenReturn(hotel)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Simple Hotel") }
                jsonPath("$.address") { value("Simple Address") }
                jsonPath("$.city") { value(null) }
                jsonPath("$.stars") { value(null) }
                jsonPath("$.email") { value("simple@hotel.com") }
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
                jsonPath("$.error") { value("Hotel con id 77 no encontrado") }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun `should create hotel when post`() {
        val request = HotelRequest(
            name = "New Hotel",
            address = "999 New Street",
            city = "Cuenca",
            stars = 4,
            email = "new@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
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
            jsonPath("$.email") { value("new@hotel.com") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should create hotel with null city and stars when post`() {
        val request = HotelRequest(
            name = "Basic Hotel",
            address = "Basic Address",
            city = null,
            stars = null,
            email = "basic@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.save(hotel)).thenReturn(hotel)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Basic Hotel") }
            jsonPath("$.address") { value("Basic Address") }
            jsonPath("$.city") { value(null) }
            jsonPath("$.stars") { value(null) }
            jsonPath("$.email") { value("basic@hotel.com") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 400 when create hotel with validation error`() {
        val request = HotelRequest(
            name = "",
            address = "999 New Street",
            city = "Cuenca",
            stars = 4,
            email = "new@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.save(hotel))
            .thenThrow(ValidationException("El nombre del hotel es requerido"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("El nombre del hotel es requerido") }
        }
    }

    @Test
    fun `should return 409 when create hotel with conflict error`() {
        val request = HotelRequest(
            name = "Existing Hotel",
            address = "Existing Address",
            city = "Cuenca",
            stars = 4,
            email = "existing@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.save(hotel))
            .thenThrow(ConflictException("Ya existe un hotel con el nombre 'Existing Hotel' en la dirección 'Existing Address'"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Ya existe un hotel con el nombre 'Existing Hotel' en la dirección 'Existing Address'") }
        }
    }

    @Test
    fun `should update hotel when put`() {
        val request = HotelRequest(
            name = "Updated Hotel",
            address = "Updated Address",
            city = "Updated City",
            stars = 5,
            email = "updated@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.update(1L, hotel)).thenReturn(hotel)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Updated Hotel") }
            jsonPath("$.address") { value("Updated Address") }
            jsonPath("$.city") { value("Updated City") }
            jsonPath("$.stars") { value(5) }
            jsonPath("$.email") { value("updated@hotel.com") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when update non-existent hotel`() {
        val request = HotelRequest(
            name = "Updated Hotel",
            address = "Updated Address",
            city = "Updated City",
            stars = 5,
            email = "updated@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.update(99L, hotel))
            .thenThrow(NotFoundException("Hotel con id 99 no encontrado"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/99") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("Hotel con id 99 no encontrado") }
        }
    }

    @Test
    fun `should return 400 when update hotel with validation error`() {
        val request = HotelRequest(
            name = "",
            address = "Updated Address",
            city = "Updated City",
            stars = 5,
            email = "updated@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.update(1L, hotel))
            .thenThrow(ValidationException("El nombre del hotel es requerido"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("El nombre del hotel es requerido") }
        }
    }

    @Test
    fun `should return 409 when update hotel with conflict error`() {
        val request = HotelRequest(
            name = "Conflicting Hotel",
            address = "Conflicting Address",
            city = "Updated City",
            stars = 5,
            email = "conflicting@hotel.com"
        )

        val hotel = Hotel(
            name = request.name,
            address = request.address,
            city = request.city,
            stars = request.stars,
            email = request.email
        )

        `when`(hotelService.update(1L, hotel))
            .thenThrow(ConflictException("Ya existe otro hotel con el nombre 'Conflicting Hotel' en la dirección 'Conflicting Address'"))

        val json = objectMapper.writeValueAsString(request)

        mockMvc.put("$BASE_URL/1") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Ya existe otro hotel con el nombre 'Conflicting Hotel' en la dirección 'Conflicting Address'") }
        }
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

    @Test
    fun `should return 404 when delete non-existent hotel`() {
        doThrow(NotFoundException("Hotel con id 99 no encontrado"))
            .`when`(hotelService).delete(99L)

        mockMvc.delete("$BASE_URL/delete/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Hotel con id 99 no encontrado") }
            }
    }

    @Test
    fun `should return 500 when unexpected error occurs`() {
        `when`(hotelService.getAll())
            .thenThrow(RuntimeException("Database connection error"))

        mockMvc.get(BASE_URL)
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.error") { value("Unexpected error: Database connection error") }
            }
    }
}