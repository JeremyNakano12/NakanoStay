package com.puce.NakanoStay.services

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.enums.BookingStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.TemplateEngine
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.mail.internet.MimeMessage

class EmailServiceTest {

    private lateinit var mailSender: JavaMailSender
    private lateinit var templateEngine: TemplateEngine
    private lateinit var mimeMessage: MimeMessage
    private lateinit var service: EmailService

    @BeforeEach
    fun setUp() {
        mailSender = mock(JavaMailSender::class.java)
        templateEngine = mock(TemplateEngine::class.java)
        mimeMessage = mock(MimeMessage::class.java)
        service = EmailService(mailSender, templateEngine)

        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        `when`(templateEngine.process(any(String::class.java), any())).thenReturn("<html>Test Email</html>")
    }

    @Test
    fun `should send booking confirmation successfully`() {
        val booking = createTestBooking()

        assertDoesNotThrow {
            service.sendBookingConfirmation(booking)
        }

        verify(mailSender, times(2)).createMimeMessage()
        verify(mailSender, times(2)).send(any(MimeMessage::class.java))
        verify(templateEngine, times(2)).process(any(String::class.java), any())
    }

    @Test
    fun `should throw exception when booking has no details`() {
        val booking = Booking(
            bookingCode = "NKS-TEST123456",
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CONFIRMED
        )

        val exception = assertThrows<RuntimeException> {
            service.sendBookingConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("La reserva no tiene detalles de habitaciones"))
        verify(mailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should throw exception when hotel is null`() {
        val booking = Booking(
            bookingCode = "NKS-TEST123456",
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CONFIRMED
        )

        val bookingDetail = mock(BookingDetail::class.java)
        val room = mock(Room::class.java)
        `when`(bookingDetail.room).thenReturn(room)
        `when`(room.hotel).thenReturn(null)

        booking.bookingDetails.add(bookingDetail)

        val exception = assertThrows<RuntimeException> {
            service.sendBookingConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("No se pudo obtener información del hotel"))
        verify(mailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should handle mail sending error`() {
        val booking = createTestBooking()

        doThrow(RuntimeException("Mail server error")).`when`(mailSender).send(any(MimeMessage::class.java))

        val exception = assertThrows<RuntimeException> {
            service.sendBookingConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("Error al enviar correo de confirmación"))
        verify(mailSender, atLeastOnce()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should handle template processing error`() {
        val booking = createTestBooking()

        `when`(templateEngine.process(any(String::class.java), any()))
            .thenThrow(RuntimeException("Template error"))

        val exception = assertThrows<RuntimeException> {
            service.sendBookingConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("Error al enviar correo de confirmación"))
        verify(templateEngine, atLeastOnce()).process(any(String::class.java), any())
    }

    @Test
    fun `should process correct templates for user and hotel`() {
        val booking = createTestBooking()

        service.sendBookingConfirmation(booking)

        verify(templateEngine).process(eq("booking-confirmation"), any())
        verify(templateEngine).process(eq("booking-notification-hotel"), any())
    }

    @Test
    fun `should send email to correct recipients`() {
        val booking = createTestBooking()

        service.sendBookingConfirmation(booking)

        verify(mailSender, times(2)).send(any(MimeMessage::class.java))
        verify(templateEngine).process(eq("booking-confirmation"), any())
        verify(templateEngine).process(eq("booking-notification-hotel"), any())
    }

    @Test
    fun `should handle booking with multiple rooms`() {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "hotel@test.com")
        val room1 = Room(hotel, "101", "Single", BigDecimal("50.00"), true)
        val room2 = Room(hotel, "102", "Double", BigDecimal("80.00"), true)

        val booking = Booking(
            bookingCode = "NKS-MULTI123456",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CONFIRMED
        )

        val bookingDetail1 = BookingDetail(
            booking = booking,
            room = room1,
            guests = 1,
            priceAtBooking = BigDecimal("250.00")
        )

        val bookingDetail2 = BookingDetail(
            booking = booking,
            room = room2,
            guests = 2,
            priceAtBooking = BigDecimal("400.00")
        )

        booking.bookingDetails.addAll(listOf(bookingDetail1, bookingDetail2))

        assertDoesNotThrow {
            service.sendBookingConfirmation(booking)
        }

        verify(mailSender, times(2)).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should send cancellation notification successfully`() {
        val booking = createTestBooking()

        assertDoesNotThrow {
            service.sendCancellationNotification(booking)
        }

        verify(mailSender, times(2)).createMimeMessage()
        verify(mailSender, times(2)).send(any(MimeMessage::class.java))
        verify(templateEngine).process(eq("booking-cancellation-guest"), any())
        verify(templateEngine).process(eq("booking-cancellation-hotel"), any())
    }

    @Test
    fun `should throw exception when cancellation booking has no details`() {
        val booking = Booking(
            bookingCode = "NKS-CANCEL123",
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CANCELLED
        )

        val exception = assertThrows<RuntimeException> {
            service.sendCancellationNotification(booking)
        }

        assertTrue(exception.message!!.contains("La reserva no tiene detalles de habitaciones"))
        verify(mailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should send booking last confirmation successfully`() {
        val booking = createTestBooking()

        assertDoesNotThrow {
            service.sendBookingLastConfirmation(booking)
        }

        verify(mailSender, times(2)).createMimeMessage()
        verify(mailSender, times(2)).send(any(MimeMessage::class.java))
        verify(templateEngine).process(eq("booking-confirmed-guest"), any())
        verify(templateEngine).process(eq("booking-confirmed-hotel"), any())
    }

    @Test
    fun `should throw exception when last confirmation booking has no details`() {
        val booking = Booking(
            bookingCode = "NKS-LASTCONF123",
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CONFIRMED
        )

        val exception = assertThrows<RuntimeException> {
            service.sendBookingLastConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("La reserva no tiene detalles de habitaciones"))
        verify(mailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should send booking completed notification successfully`() {
        val booking = createTestBooking()

        assertDoesNotThrow {
            service.sendBookingCompletedNotification(booking)
        }

        verify(mailSender, times(1)).createMimeMessage()
        verify(mailSender, times(1)).send(any(MimeMessage::class.java))
        verify(templateEngine).process(eq("booking-completed-guest"), any())
    }

    @Test
    fun `should throw exception when completed booking has no details`() {
        val booking = Booking(
            bookingCode = "NKS-COMPLETED123",
            guestName = "Test User",
            guestDni = "1234567890",
            guestEmail = "test@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.COMPLETED
        )

        val exception = assertThrows<RuntimeException> {
            service.sendBookingCompletedNotification(booking)
        }

        assertTrue(exception.message!!.contains("La reserva no tiene detalles de habitaciones"))
        verify(mailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should handle cancellation mail sending error`() {
        val booking = createTestBooking()

        doThrow(RuntimeException("Mail server error")).`when`(mailSender).send(any(MimeMessage::class.java))

        val exception = assertThrows<RuntimeException> {
            service.sendCancellationNotification(booking)
        }

        assertTrue(exception.message!!.contains("Error al enviar correos de cancelación"))
        verify(mailSender, atLeastOnce()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should handle last confirmation mail sending error`() {
        val booking = createTestBooking()

        doThrow(RuntimeException("Mail server error")).`when`(mailSender).send(any(MimeMessage::class.java))

        val exception = assertThrows<RuntimeException> {
            service.sendBookingLastConfirmation(booking)
        }

        assertTrue(exception.message!!.contains("Error al enviar correo de confirmación"))
        verify(mailSender, atLeastOnce()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should handle completed mail sending error`() {
        val booking = createTestBooking()

        doThrow(RuntimeException("Mail server error")).`when`(mailSender).send(any(MimeMessage::class.java))

        val exception = assertThrows<RuntimeException> {
            service.sendBookingCompletedNotification(booking)
        }

        assertTrue(exception.message!!.contains("Error al enviar correo de confirmación"))
        verify(mailSender, atLeastOnce()).send(any(MimeMessage::class.java))
    }

    private fun createTestBooking(): Booking {
        val hotel = Hotel("Test Hotel", "Test Address", "Test City", 4, "hotel@test.com")
        val room = Room(hotel, "101", "Single", BigDecimal("50.00"), true)

        val booking = Booking(
            bookingCode = "NKS-TEST123456",
            guestName = "John Doe",
            guestDni = "1234567890",
            guestEmail = "john@test.com",
            guestPhone = "0999999999",
            bookingDate = LocalDateTime.now(),
            checkIn = LocalDate.of(2025, 1, 15),
            checkOut = LocalDate.of(2025, 1, 20),
            status = BookingStatus.CONFIRMED
        )

        val bookingDetail = BookingDetail(
            booking = booking,
            room = room,
            guests = 2,
            priceAtBooking = BigDecimal("250.00")
        )

        booking.bookingDetails.add(bookingDetail)
        return booking
    }
}