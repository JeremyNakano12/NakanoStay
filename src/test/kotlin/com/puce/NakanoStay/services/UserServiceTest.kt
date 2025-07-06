package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.repositories.UserRepository
import java.util.*
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        service = UserService(userRepository)
    }

    @Test
    fun `should return all users`() {
        val users = listOf(
            User(
                name = "Jeremy",
                dni = "9999999999",
                email = "jmarin@puce.edu.ec",
                phone = "0911223344"
            ),
            User(
                name = "Akimi",
                dni = "9999999991",
                email = "alazaro@puce.edu.ec",
                phone = "0998877665"
            )
        )

        `when`(userRepository.findAll())
            .thenReturn(users)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("Jeremy", result[0].name)
        assertEquals("Akimi", result[1].name)
    }

    @Test
    fun `should get a user by id`() {
        val user = User(
            name = "Anghelo",
            dni = "9999999993",
            email = "aamontiel@puce.edu.ec",
            phone = "999999999")

        `when`(userRepository.findById(1L))
            .thenReturn(Optional.of(user))

        val result = service.getById(1L)

        assertEquals("Anghelo", result.name)
        assertEquals("aamontiel@puce.edu.ec", result.email)
    }

    @Test
    fun `should throw NotFoundException when user not found by id`() {
        `when`(userRepository.findById(42L))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(42L)
        }

        assertEquals("Usuario con id 42 no encontrado", exception.message)
    }

    @Test
    fun `should save a user`() {
        val user = User(
            name = "John",
            dni = "9999999994",
            email = "jasan@puce.edu.ec",
            phone = "0912312312")

        `when`(userRepository.save(user))
            .thenReturn(user)

        val savedUser = service.save(user)

        assertEquals("John", savedUser.name)
    }

    @Test
    fun `should delete a user by id`() {
        `when`(userRepository.existsById(10L))
            .thenReturn(true)

        service.delete(10L)

        verify(userRepository).deleteById(10L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent user`() {
        `when`(userRepository.existsById(77L))
            .thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(77L)
        }

        assertEquals("Usuario con id 77 no encontrado", exception.message)
        verify(userRepository, never()).deleteById(anyLong())
    }

}