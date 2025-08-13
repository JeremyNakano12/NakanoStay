package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.repositories.RoomRepository
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class RoomServiceTest {
    private lateinit var roomRepository: RoomRepository
    private lateinit var service: RoomService

    @BeforeEach
    fun setUp() {
        roomRepository = mock(RoomRepository::class.java)
        service = RoomService(roomRepository)
    }

    @Test
    fun `should save a valid room`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        `when`(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(false)
        `when`(roomRepository.save(room)).thenReturn(room)

        val savedRoom = service.save(room)

        assertEquals("101", savedRoom.roomNumber)
        verify(roomRepository).save(room)
    }

    @Test
    fun `should throw ValidationException when room number is blank`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "", "Single", BigDecimal("50.00"), true)

        val exception = assertThrows<ValidationException> {
            service.save(room)
        }

        assertEquals("El número de habitación es requerido", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when room number is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "12345678901", "Single", BigDecimal("50.00"), true) // 11 caracteres

        val exception = assertThrows<ValidationException> {
            service.save(room)
        }

        assertEquals("El número de habitación no puede tener más de 10 caracteres", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when price is negative`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("-10.00"), true)

        val exception = assertThrows<ValidationException> {
            service.save(room)
        }

        assertEquals("El precio por noche no puede ser negativo", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when room type is too long`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val longRoomType = "a".repeat(51) // 51 caracteres
        val room = Room(hotel, "101", longRoomType, BigDecimal("50.00"), true)

        val exception = assertThrows<ValidationException> {
            service.save(room)
        }

        assertEquals("El tipo de habitación no puede tener más de 50 caracteres", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should throw ConflictException when room number already exists in hotel`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        `when`(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(true)

        val exception = assertThrows<ConflictException> {
            service.save(room)
        }

        assertEquals("Ya existe una habitación con el número '101' en este hotel", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should get rooms by hotel`() {
        val hotel = Hotel("Beach Hotel", "Beach Address", "Coastal City", 5, "beach@hotel.com")
        val rooms = listOf(
            Room(hotel, "301", "Suite", BigDecimal("150.00"), true),
            Room(hotel, "302", "Suite", BigDecimal("150.00"), true)
        )

        `when`(roomRepository.findByHotelId(1L)).thenReturn(rooms)

        val result = service.getByHotel(1L)

        assertEquals(2, result.size)
        assertEquals("301", result[0].roomNumber)
        assertEquals("302", result[1].roomNumber)
    }

    @Test
    fun `should get a room by id`() {
        val hotel = Hotel("Mountain Hotel", "Mountain Address", "Highland City", 3, "mountain@hotel.com")
        val room = Room(hotel, "A1", "Deluxe", BigDecimal("120.00"), true)

        `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))

        val result = service.getById(1L)

        assertEquals("A1", result.roomNumber)
        assertEquals("Deluxe", result.roomType)
    }

    @Test
    fun `should throw NotFoundException when room not found by id`() {
        `when`(roomRepository.findById(75L)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(75L)
        }

        assertEquals("Habitación con id 75 no encontrada", exception.message)
    }

    @Test
    fun `should update room successfully`() {
        val hotel = Hotel("Updated Hotel", "Updated Address", "Updated City", 4, "updated@hotel.com").apply { id = 1L }
        val room = Room(hotel, "102", "Double", BigDecimal("80.00"), true)

        `when`(roomRepository.existsById(1L)).thenReturn(true)
        `when`(roomRepository.existsByHotelIdAndRoomNumberAndIdNot(1L, "102", 1L)).thenReturn(false)
        `when`(roomRepository.save(any<Room>())).thenReturn(room)

        val updatedRoom = service.update(1L, room)

        assertEquals("102", updatedRoom.roomNumber)
        verify(roomRepository).save(any<Room>())
    }

    @Test
    fun `should throw NotFoundException when updating non-existent room`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com").apply { id = 1L }
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        `when`(roomRepository.existsById(99L)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.update(99L, room)
        }

        assertEquals("Habitación con id 99 no encontrada", exception.message)
        verify(roomRepository, never()).save(any())
    }

    @Test
    fun `should delete room successfully`() {
        `when`(roomRepository.existsById(7L)).thenReturn(true)

        service.delete(7L)

        verify(roomRepository).deleteById(7L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent room`() {
        `when`(roomRepository.existsById(200L)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(200L)
        }

        assertEquals("Habitación con id 200 no encontrada", exception.message)
        verify(roomRepository, never()).deleteById(any())
    }
}