package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
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
            Hotel(
                name = "Hotel Paradise",
                address = "123 Main Street, Downtown",
                city = "Quito",
                stars = 5,
                email = "paradise@hotel.com"
            ),
            Hotel(
                name = "Budget Inn",
                address = "456 Side Street, North Zone",
                city = "Guayaquil",
                stars = 3,
                email = "budget@inn.com"
            )
        )

        `when`(hotelRepository.findAll())
            .thenReturn(hotels)

        val result = service.getAll()

        assertEquals(2, result.size)
        assertEquals("Hotel Paradise", result[0].name)
        assertEquals("Budget Inn", result[1].name)
        assertEquals(5, result[0].stars)
        assertEquals(3, result[1].stars)
        assertEquals("paradise@hotel.com", result[0].email)
        assertEquals("budget@inn.com", result[1].email)
    }

    @Test
    fun `should get a hotel by id`() {
        val hotel = Hotel(
            name = "Grand Hotel",
            address = "789 Luxury Avenue, Historic Center",
            city = "Cuenca",
            stars = 4,
            email = "grand@hotel.com"
        )

        `when`(hotelRepository.findById(1L))
            .thenReturn(Optional.of(hotel))

        val result = service.getById(1L)

        assertEquals("Grand Hotel", result.name)
        assertEquals("789 Luxury Avenue, Historic Center", result.address)
        assertEquals("Cuenca", result.city)
        assertEquals(4, result.stars)
        assertEquals("grand@hotel.com", result.email)
    }

    @Test
    fun `should throw NotFoundException when hotel not found by id`() {
        `when`(hotelRepository.findById(50L))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.getById(50L)
        }

        assertEquals("Hotel con id 50 no encontrado", exception.message)
    }

    @Test
    fun `should save a hotel`() {
        val hotel = Hotel(
            name = "New Hotel",
            address = "999 New Street, Modern District",
            city = "Manta",
            stars = 4,
            email = "new@hotel.com"
        )

        `when`(hotelRepository.save(hotel))
            .thenReturn(hotel)

        val savedHotel = service.save(hotel)

        assertEquals("New Hotel", savedHotel.name)
        assertEquals("999 New Street, Modern District", savedHotel.address)
        assertEquals("Manta", savedHotel.city)
        assertEquals(4, savedHotel.stars)
        assertEquals("new@hotel.com", savedHotel.email)
    }

    @Test
    fun `should delete a hotel by id`() {
        `when`(hotelRepository.existsById(3L))
            .thenReturn(true)

        service.delete(3L)

        verify(hotelRepository).deleteById(3L)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent hotel`() {
        `when`(hotelRepository.existsById(100L))
            .thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.delete(100L)
        }

        assertEquals("Hotel con id 100 no encontrado", exception.message)
        verify(hotelRepository, never()).deleteById(anyLong())
    }
}