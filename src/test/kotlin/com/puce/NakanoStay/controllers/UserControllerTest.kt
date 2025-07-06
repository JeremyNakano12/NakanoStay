package com.puce.NakanoStay.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.models.requests.UserRequest
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.UserService
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

@WebMvcTest(UserController::class)
@Import(UserMockConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    val BASE_URL = Routes.BASE_URL + Routes.USERS

    @Test
    fun `should return all users when get all`() {
        val users = listOf(
            User("John Doe", "1234567890", "john@puce.edu.ec", "0999999999"),
            User("Jane Smith", "0987654321", "jane@puce.edu.ec", "0988888888")
        )

        `when`(userService.getAll()).thenReturn(users)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("John Doe") }
                jsonPath("$[0].email") { value("john@puce.edu.ec") }
                jsonPath("$[1].name") { value("Jane Smith") }
                jsonPath("$[1].email") { value("jane@puce.edu.ec") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return user when get by id`() {
        val user = User("Alice Johnson", "1111111111", "alice@puce.edu.ec", "0977777777")

        `when`(userService.getById(1L)).thenReturn(user)

        val result = mockMvc.get("$BASE_URL/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Alice Johnson") }
                jsonPath("$.email") { value("alice@puce.edu.ec") }
                jsonPath("$.dni") { value("1111111111") }
                jsonPath("$.phone") { value("0977777777") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should return 404 when get user by non-existent id`() {
        val userId = 99L

        `when`(userService.getById(userId))
            .thenThrow(NotFoundException("Usuario con id $userId no encontrado"))

        val result = mockMvc.get("$BASE_URL/99")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun `should create user when post`() {
        val request = UserRequest(
            name = "Bob Wilson",
            dni = "2222222222",
            email = "bob@puce.edu.ec",
            phone = "0966666666"
        )

        val user = User(
            name = request.name,
            dni = request.dni,
            email = request.email,
            phone = request.phone
        )

        `when`(userService.save(user)).thenReturn(user)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Bob Wilson") }
            jsonPath("$.dni") { value("2222222222") }
            jsonPath("$.email") { value("bob@puce.edu.ec") }
            jsonPath("$.phone") { value("0966666666") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun `should delete user when delete by id`() {
        val result = mockMvc.delete("$BASE_URL/delete/5")
            .andExpect {
                status { isNoContent() }
            }.andReturn()

        verify(userService).delete(5L)
        assertEquals(204, result.response.status)
    }
}

@TestConfiguration
class UserMockConfig {
    @Bean
    fun userService(): UserService = mock(UserService::class.java)
}