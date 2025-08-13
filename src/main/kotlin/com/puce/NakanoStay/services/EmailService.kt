package com.puce.NakanoStay.services

import com.puce.NakanoStay.models.entities.Booking
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendBookingConfirmation(booking: Booking) {
        try {
            logger.info("Iniciando envío de correo de confirmación para reserva código: ${booking.bookingCode}")

            if (booking.bookingDetails.isEmpty()) {
                throw RuntimeException("La reserva no tiene detalles de habitaciones")
            }

            val hotel = booking.bookingDetails.firstOrNull()?.room?.hotel
                ?: throw RuntimeException("No se pudo obtener información del hotel")

            logger.info("Enviando correo a huésped: ${booking.guestEmail}")
            sendUserConfirmation(booking)

            logger.info("Enviando correo a hotel: ${hotel.email}")
            sendHotelNotification(booking, hotel.email)

            logger.info("Correos enviados exitosamente para reserva código: ${booking.bookingCode}")

        } catch (e: Exception) {
            logger.error("Error al enviar correo de confirmación para reserva código: ${booking.bookingCode}", e)
            throw RuntimeException("Error al enviar correo de confirmación: ${e.message}", e)
        }
    }

    private fun sendUserConfirmation(booking: Booking) {
        try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            val context = Context().apply {
                setVariable("booking", booking)
                setVariable("hotel", booking.bookingDetails.firstOrNull()?.room?.hotel)
                setVariable("totalGuests", booking.bookingDetails.sumOf { it.guests })
            }

            val htmlContent = templateEngine.process("booking-confirmation", context)

            helper.setTo(booking.guestEmail)
            helper.setSubject("Confirmación de Reserva - NakanoStay")
            helper.setText(htmlContent, true)
            helper.setFrom("noreply@nakanostay.com")

            mailSender.send(mimeMessage)

            logger.info("Correo de confirmación enviado exitosamente a: ${booking.guestEmail}")

        } catch (e: Exception) {
            logger.error("Error al enviar correo al huésped: ${booking.guestEmail}", e)
            throw RuntimeException("Error al enviar correo al huésped: ${e.message}", e)
        }
    }

    private fun sendHotelNotification(booking: Booking, hotelEmail: String) {
        try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            val context = Context().apply {
                setVariable("booking", booking)
                setVariable("hotel", booking.bookingDetails.firstOrNull()?.room?.hotel)
                setVariable("totalGuests", booking.bookingDetails.sumOf { it.guests })
            }

            val htmlContent = templateEngine.process("booking-notification-hotel", context)

            helper.setTo(hotelEmail)
            helper.setSubject("Nueva Reserva Recibida - NakanoStay")
            helper.setText(htmlContent, true)
            helper.setFrom("noreply@nakanostay.com")

            mailSender.send(mimeMessage)

            logger.info("Notificación enviada exitosamente al hotel: $hotelEmail")

        } catch (e: Exception) {
            logger.error("Error al enviar notificación al hotel: $hotelEmail", e)
            throw RuntimeException("Error al enviar notificación al hotel: ${e.message}", e)
        }
    }

    fun sendCancellationNotification(booking: Booking) {
        try {
            logger.info("Enviando notificación de cancelación para reserva: ${booking.bookingCode}")

            if (booking.bookingDetails.isEmpty()) {
                throw RuntimeException("La reserva no tiene detalles de habitaciones")
            }

            val hotel = booking.bookingDetails.firstOrNull()?.room?.hotel
                ?: throw RuntimeException("No se pudo obtener información del hotel")

            logger.info("Enviando correo de cancelación a huésped: ${booking.guestEmail}")
            sendCancellationEmailToGuest(booking)

            logger.info("Enviando correo de cancelación a hotel: ${hotel.email}")
            sendCancellationEmailToHotel(booking, hotel.email)

            logger.info("Correos de cancelación enviados exitosamente para reserva: ${booking.bookingCode}")

        } catch (e: Exception) {
            logger.error("Error al enviar correos de cancelación para reserva: ${booking.bookingCode}", e)
            throw RuntimeException("Error al enviar correos de cancelación: ${e.message}", e)
        }
    }

    private fun sendCancellationEmailToGuest(booking: Booking) {
        try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            val context = Context().apply {
                setVariable("booking", booking)
                setVariable("hotel", booking.bookingDetails.firstOrNull()?.room?.hotel)
                setVariable("totalGuests", booking.bookingDetails.sumOf { it.guests })
            }

            val htmlContent = templateEngine.process("booking-cancellation-guest", context)

            helper.setTo(booking.guestEmail)
            helper.setSubject("Reserva Cancelada - NakanoStay")
            helper.setText(htmlContent, true)
            helper.setFrom("noreply@nakanostay.com")

            mailSender.send(mimeMessage)

            logger.info("Correo de cancelación enviado exitosamente al huésped: ${booking.guestEmail}")

        } catch (e: Exception) {
            logger.error("Error al enviar correo de cancelación al huésped: ${booking.guestEmail}", e)
            throw RuntimeException("Error al enviar correo de cancelación al huésped: ${e.message}", e)
        }
    }

    private fun sendCancellationEmailToHotel(booking: Booking, hotelEmail: String) {
        try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            val context = Context().apply {
                setVariable("booking", booking)
                setVariable("hotel", booking.bookingDetails.firstOrNull()?.room?.hotel)
                setVariable("totalGuests", booking.bookingDetails.sumOf { it.guests })
            }

            val htmlContent = templateEngine.process("booking-cancellation-hotel", context)

            helper.setTo(hotelEmail)
            helper.setSubject("Reserva Cancelada - NakanoStay")
            helper.setText(htmlContent, true)
            helper.setFrom("noreply@nakanostay.com")

            mailSender.send(mimeMessage)

            logger.info("Notificación de cancelación enviada exitosamente al hotel: $hotelEmail")

        } catch (e: Exception) {
            logger.error("Error al enviar notificación de cancelación al hotel: $hotelEmail", e)
            throw RuntimeException("Error al enviar notificación de cancelación al hotel: ${e.message}", e)
        }
    }
}