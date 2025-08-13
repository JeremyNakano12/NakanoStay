package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.repositories.HotelRepository
import java.util.*
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class HotelServiceTest {
    private lateinit var hotelRepository: HotelRepository
    private lateinit var service: HotelService

    @BeforeEach
    fun setUp() {
        hotelRepository = mock(HotelRepository::class.java)
        service = HotelService(hotelRepository)
    }

    @Test
    fun `should return all hotels`() {
        val hotels = listOf(
            Hotel("Hotel Paradise", "123 Main Street", "Quito", 5, "paradise@hotel.com"),
            Hotel("Budget Inn", "456 Side Street", "Guayaquil", 3, "budget@inn.com")
        )

        `when`(hotelRepository.findAll()).thenReturn(hotels)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("Hotel Paradise", result[0].name)
        assertEquals("Budget Inn", result[1].name)
    }

    @Test
    fun `should save a valid hotel`() {
        val hotel = Hotel("New Hotel", "999 New Street", "Manta", 4, "new@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
        `when`(hotelRepository.save(hotel)).thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("New Hotel", savedHotel.name)
        verify(hotelRepository).save(hotel)
    }

    @Test
    fun `should throw ValidationException when hotel name is blank`() {
        val hotel = Hotel("", "123 Main Street", "Quito", 5, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("El nombre del hotel es requerido", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when hotel address is blank`() {
        val hotel = Hotel("Test Hotel", "", "Quito", 5, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("La dirección del hotel es requerida", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when email is invalid`() {
        val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 5, "invalid-email")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("El formato del email es inválido", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when stars are negative`() {
        val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", -1, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("Las estrellas del hotel no pueden ser negativas", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when stars are more than 5`() {
        val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 6, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("Las estrellas del hotel no pueden ser más de 5", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when hotel with same name and address exists`() {
        val hotel = Hotel("Existing Hotel", "123 Main Street", "Quito", 4, "new@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.save(hotel)
        }

        assertEquals("Ya existe un hotel con el nombre 'Existing Hotel' en la dirección '123 Main Street'", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when hotel with same email exists`() {
        val hotel = Hotel("New Hotel", "999 New Street", "Manta", 4, "existing@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.save(hotel)
        }

        assertEquals("Ya existe un hotel registrado con el email 'existing@hotel.com'", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should update hotel successfully`() {
        val hotel = Hotel("Updated Hotel", "Updated Address", "Cuenca", 4, "updated@hotel.com")

        `when`(hotelRepository.existsById(1L)).thenReturn(true)
        `when`(hotelRepository.existsByNameAndAddressAndIdNot(hotel.name, hotel.address, 1L)).thenReturn(false)
        `when`(hotelRepository.existsByEmailAndIdNot(hotel.email, 1L)).thenReturn(false)
        `when`(hotelRepository.save(any<Hotel>())).thenReturn(hotel)

        val updatedHotel = service.update(1L, hotel)

        assertEquals("Updated Hotel", updatedHotel.name)
        verify(hotelRepository).save(any<Hotel>())
    }

    @Test
    fun `should throw NotFoundException when updating non-existent hotel`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")

        `when`(hotelRepository.existsById(99L)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.update(99L, hotel)
        }

        assertEquals("Hotel con id 99 no encontrado", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should delete hotel successfully`() {
        `when`(hotelRepository.existsById(1L)).thenReturn(true)

        service.delete(1L)

        verify(hotelRepository).deleteById(1L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent hotel`() {
        `when`(hotelRepository.existsById(99L)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(99L)
        }

        assertEquals("Hotel con id 99 no encontrado", exception.message)
        verify(hotelRepository, never()).deleteById(any())
    }
}