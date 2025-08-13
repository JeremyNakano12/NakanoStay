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
    fun `should return empty list when no hotels exist`() {
        `when`(hotelRepository.findAll()).thenReturn(emptyList())

        val result = service.getAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return hotel by id`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Quito", 4, "test@hotel.com")
        `when`(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel))

        val result = service.getById(1L)

        assertEquals("Test Hotel", result.name)
        assertEquals("Test Address", result.address)
    }

    @Test
    fun `should throw NotFoundException when hotel not found by id`() {
        `when`(hotelRepository.findById(99L)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(99L)
        }

        assertEquals("Hotel con id 99 no encontrado", exception.message)
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
    fun `should save hotel with null city`() {
        val hotel = Hotel("New Hotel", "999 New Street", null, 4, "new@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
        `when`(hotelRepository.save(hotel)).thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("New Hotel", savedHotel.name)
        assertNull(savedHotel.city)
        verify(hotelRepository).save(hotel)
    }

    @Test
    fun `should save hotel with null stars`() {
        val hotel = Hotel("New Hotel", "999 New Street", "Manta", null, "new@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
        `when`(hotelRepository.save(hotel)).thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("New Hotel", savedHotel.name)
        assertNull(savedHotel.stars)
        verify(hotelRepository).save(hotel)
    }

    @Test
    fun `should save hotel with 0 stars`() {
        val hotel = Hotel("New Hotel", "999 New Street", "Manta", 0, "new@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
        `when`(hotelRepository.save(hotel)).thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("New Hotel", savedHotel.name)
        assertEquals(0, savedHotel.stars)
        verify(hotelRepository).save(hotel)
    }

    @Test
    fun `should save hotel with 5 stars`() {
        val hotel = Hotel("Luxury Hotel", "999 New Street", "Manta", 5, "luxury@hotel.com")

        `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
        `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
        `when`(hotelRepository.save(hotel)).thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("Luxury Hotel", savedHotel.name)
        assertEquals(5, savedHotel.stars)
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
    fun `should throw ValidationException when hotel name is too long`() {
        val longName = "a".repeat(101)
        val hotel = Hotel(longName, "123 Main Street", "Quito", 5, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("El nombre del hotel no puede tener más de 100 caracteres", exception.message)
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
    fun `should throw ValidationException when email is blank`() {
        val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 5, "")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("El email del hotel es requerido", exception.message)
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
    fun `should throw ValidationException when email is too long`() {
        val longEmail = "a".repeat(91) + "@hotel.com" // Más de 100 caracteres
        val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 5, longEmail)

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("El email no puede tener más de 100 caracteres", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when city is too long`() {
        val longCity = "a".repeat(101)
        val hotel = Hotel("Test Hotel", "123 Main Street", longCity, 5, "test@hotel.com")

        val exception = assertThrows<ValidationException> {
            service.save(hotel)
        }

        assertEquals("La ciudad no puede tener más de 100 caracteres", exception.message)
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
    fun `should validate various email formats correctly`() {
        val validEmails = listOf(
            "test@hotel.com",
            "admin@my-hotel.org",
            "user123@example.co.uk",
            "hotel+booking@example.com"
        )

        validEmails.forEach { email ->
            val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 4, email)
            `when`(hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)).thenReturn(false)
            `when`(hotelRepository.existsByEmail(hotel.email)).thenReturn(false)
            `when`(hotelRepository.save(hotel)).thenReturn(hotel)

            assertDoesNotThrow {
                service.save(hotel)
            }
        }

        val invalidEmails = listOf(
            "invalid-email",
            "@hotel.com",
            "test@",
            "test.hotel.com",
            "test@hotel",
            ""
        )

        invalidEmails.forEach { email ->
            val hotel = Hotel("Test Hotel", "123 Main Street", "Quito", 4, email)

            assertThrows<ValidationException> {
                service.save(hotel)
            }
        }
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
    fun `should update hotel with null city and stars`() {
        val hotel = Hotel("Updated Hotel", "Updated Address", null, null, "updated@hotel.com")

        `when`(hotelRepository.existsById(1L)).thenReturn(true)
        `when`(hotelRepository.existsByNameAndAddressAndIdNot(hotel.name, hotel.address, 1L)).thenReturn(false)
        `when`(hotelRepository.existsByEmailAndIdNot(hotel.email, 1L)).thenReturn(false)
        `when`(hotelRepository.save(any<Hotel>())).thenReturn(hotel)

        val updatedHotel = service.update(1L, hotel)

        assertEquals("Updated Hotel", updatedHotel.name)
        assertNull(updatedHotel.city)
        assertNull(updatedHotel.stars)
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
    fun `should throw ConflictException when updating with duplicate name and address`() {
        val hotel = Hotel("Existing Hotel", "Existing Address", "Test City", 4, "test@hotel.com")

        `when`(hotelRepository.existsById(1L)).thenReturn(true)
        `when`(hotelRepository.existsByNameAndAddressAndIdNot(hotel.name, hotel.address, 1L)).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.update(1L, hotel)
        }

        assertEquals("Ya existe otro hotel con el nombre 'Existing Hotel' en la dirección 'Existing Address'", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when updating with duplicate email`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "existing@hotel.com")

        `when`(hotelRepository.existsById(1L)).thenReturn(true)
        `when`(hotelRepository.existsByNameAndAddressAndIdNot(hotel.name, hotel.address, 1L)).thenReturn(false)
        `when`(hotelRepository.existsByEmailAndIdNot(hotel.email, 1L)).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.update(1L, hotel)
        }

        assertEquals("Ya existe otro hotel registrado con el email 'existing@hotel.com'", exception.message)
        verify(hotelRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when updating with invalid data`() {
        val hotel = Hotel("", "Test Address", "Test City", 4, "test@hotel.com")

        `when`(hotelRepository.existsById(1L)).thenReturn(true)

        val exception = assertThrows<ValidationException> {
            service.update(1L, hotel)
        }

        assertEquals("El nombre del hotel es requerido", exception.message)
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