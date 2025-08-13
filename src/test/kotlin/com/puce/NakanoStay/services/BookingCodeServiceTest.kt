package com.puce.NakanoStay.services

import com.puce.NakanoStay.repositories.BookingRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class BookingCodeServiceTest {
    private lateinit var bookingRepository: BookingRepository
    private lateinit var service: BookingCodeService

    @BeforeEach
    fun setUp() {
        bookingRepository = mock(BookingRepository::class.java)
        service = BookingCodeService(bookingRepository)
    }

    @Test
    fun `should generate unique booking code successfully`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(false)

        val code = service.generateUniqueBookingCode()

        assertNotNull(code)
        assertTrue(code.startsWith("NKS-"))
        assertEquals(16, code.length)
        verify(bookingRepository).existsByBookingCode(code)
    }

    @Test
    fun `should generate different codes on multiple calls`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(false)

        val code1 = service.generateUniqueBookingCode()
        val code2 = service.generateUniqueBookingCode()

        assertNotEquals(code1, code2)
        assertTrue(code1.startsWith("NKS-"))
        assertTrue(code2.startsWith("NKS-"))
    }

    @Test
    fun `should retry when code already exists and then succeed`() {
        `when`(bookingRepository.existsByBookingCode(anyString()))
            .thenReturn(true)
            .thenReturn(false)

        val code = service.generateUniqueBookingCode()

        assertNotNull(code)
        assertTrue(code.startsWith("NKS-"))
        verify(bookingRepository, times(2)).existsByBookingCode(anyString())
    }

    @Test
    fun `should throw exception when unable to generate unique code after max attempts`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(true)

        val exception = assertThrows<RuntimeException> {
            service.generateUniqueBookingCode()
        }

        assertTrue(exception.message!!.contains("No se pudo generar un código único después de"))
        verify(bookingRepository, times(50)).existsByBookingCode(anyString())
    }

    @Test
    fun `generated code should have correct format`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(false)

        val code = service.generateUniqueBookingCode()

        val regex = "^NKS-[A-Z0-9]{6}\\d{6}$".toRegex()
        assertTrue(code.matches(regex), "Code $code does not match expected format")
    }

    @Test
    fun `should include current date in generated code`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(false)

        val code = service.generateUniqueBookingCode()

        val datePart = code.takeLast(6)
        assertTrue(datePart.matches("\\d{6}".toRegex()))

        val year = datePart.substring(0, 2).toInt()
        val month = datePart.substring(2, 4).toInt()
        val day = datePart.substring(4, 6).toInt()

        assertTrue(year >= 0 && year <= 99)
        assertTrue(month >= 1 && month <= 12)
        assertTrue(day >= 1 && day <= 31)
    }

    @Test
    fun `should generate codes with only valid characters`() {
        `when`(bookingRepository.existsByBookingCode(anyString())).thenReturn(false)

        repeat(10) {
            val code = service.generateUniqueBookingCode()

            val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-"
            code.forEach { char ->
                assertTrue(char in validChars, "Invalid character $char in code $code")
            }
        }
    }
}