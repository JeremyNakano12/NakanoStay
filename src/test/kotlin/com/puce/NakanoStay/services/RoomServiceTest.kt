package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
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
    fun `should return all rooms`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "test@hotel.com")
        val rooms = listOf(
            Room(
                hotel = hotel,
                roomNumber = "101",
                roomType = "Single",
                pricePerNight = BigDecimal("50.00"),
                isAvailable = true
            ),
            Room(
                hotel = hotel,
                roomNumber = "201",
                roomType = "Double",
                pricePerNight = BigDecimal("80.00"),
                isAvailable = false
            )
        )

        `when`(roomRepository.findAll())
            .thenReturn(rooms)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("101", result[0].roomNumber)
        assertEquals("201", result[1].roomNumber)
        assertEquals("Single", result[0].roomType)
        assertEquals("Double", result[1].roomType)
        assertTrue(result[0].isAvailable)
        assertFalse(result[1].isAvailable)
    }

    @Test
    fun `should get rooms by hotel id`() {
        val hotel = Hotel("Beach Hotel", "Beach Address", "Coastal City", 5, "beach@hotel.com")
        val rooms = listOf(
            Room(
                hotel = hotel,
                roomNumber = "301",
                roomType = "Suite",
                pricePerNight = BigDecimal("150.00"),
                isAvailable = true
            ),
            Room(
                hotel = hotel,
                roomNumber = "302",
                roomType = "Suite",
                pricePerNight = BigDecimal("150.00"),
                isAvailable = true
            )
        )

        `when`(roomRepository.findByHotelId(1L))
            .thenReturn(rooms)

        val result = service.getByHotel(1L)

        assertEquals(2, result.size)
        assertEquals("301", result[0].roomNumber)
        assertEquals("302", result[1].roomNumber)
        assertEquals("Suite", result[0].roomType)
        assertEquals("Suite", result[1].roomType)
    }

    @Test
    fun `should get a room by id`() {
        val hotel = Hotel("Mountain Hotel", "Mountain Address", "Highland City", 3, "mountain@hotel.com")
        val room = Room(
            hotel = hotel,
            roomNumber = "A1",
            roomType = "Deluxe",
            pricePerNight = BigDecimal("120.00"),
            isAvailable = true
        )

        `when`(roomRepository.findById(1L))
            .thenReturn(Optional.of(room))

        val result = service.getById(1L)

        assertEquals("A1", result.roomNumber)
        assertEquals("Deluxe", result.roomType)
        assertEquals(BigDecimal("120.00"), result.pricePerNight)
        assertTrue(result.isAvailable)
    }

    @Test
    fun `should throw NotFoundException when room not found by id`() {
        `when`(roomRepository.findById(75L))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(75L)
        }

        assertEquals("Habitación con id 75 no encontrada", exception.message)
    }

    @Test
    fun `should save a room`() {
        val hotel = Hotel("City Hotel", "City Address", "Urban City", 4, "city@hotel.com")
        val room = Room(
            hotel = hotel,
            roomNumber = "B5",
            roomType = "Standard",
            pricePerNight = BigDecimal("90.00"),
            isAvailable = true
        )

        `when`(roomRepository.save(room))
            .thenReturn(room)

        val savedRoom = service.save(room)

        assertEquals("B5", savedRoom.roomNumber)
        assertEquals("Standard", savedRoom.roomType)
        assertEquals(BigDecimal("90.00"), savedRoom.pricePerNight)
        assertTrue(savedRoom.isAvailable)
    }

    @Test
    fun `should delete a room by id`() {
        `when`(roomRepository.existsById(7L))
            .thenReturn(true)

        service.delete(7L)

        verify(roomRepository).deleteById(7L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent room`() {
        `when`(roomRepository.existsById(200L))
            .thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(200L)
        }

        assertEquals("Habitación con id 200 no encontrada", exception.message)
        verify(roomRepository, never()).deleteById(anyLong())
    }
}