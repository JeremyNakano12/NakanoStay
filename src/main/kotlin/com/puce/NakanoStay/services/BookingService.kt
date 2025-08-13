package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.repositories.BookingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingService(
    private val bookingRepository: BookingRepository
) {

    fun getAll(): List<Booking> = bookingRepository.findAll()

    fun getById(id: Long): Booking =
        bookingRepository.findById(id).orElseThrow {
            NotFoundException("Reserva con id $id no encontrada")
        }

    fun getByCodeAndDni(bookingCode: String, guestDni: String): Booking =
        bookingRepository.findByBookingCodeAndGuestDni(bookingCode, guestDni).orElseThrow {
            NotFoundException("Reserva no encontrada o datos incorrectos")
        }

    @Transactional
    fun save(booking: Booking): Booking {
        return bookingRepository.save(booking)
    }

    @Transactional
    fun cancelBooking(bookingCode: String, guestDni: String): Booking {
        val booking = getByCodeAndDni(bookingCode, guestDni)

        if (booking.status == BookingStatus.CANCELLED) {
            throw RuntimeException("La reserva ya está cancelada")
        }

        if (booking.status == BookingStatus.COMPLETED) {
            throw RuntimeException("No se puede cancelar una reserva completada")
        }

        val cancelledBooking = Booking(
            bookingCode = booking.bookingCode,
            guestName = booking.guestName,
            guestDni = booking.guestDni,
            guestEmail = booking.guestEmail,
            guestPhone = booking.guestPhone,
            bookingDate = booking.bookingDate,
            checkIn = booking.checkIn,
            checkOut = booking.checkOut,
            status = BookingStatus.CANCELLED
        )

        cancelledBooking.id = booking.id

        cancelledBooking.bookingDetails.addAll(booking.bookingDetails)

        return bookingRepository.save(cancelledBooking)
    }

    fun delete(id: Long) {
        if (!bookingRepository.existsById(id)) {
            throw NotFoundException("Reserva con id $id no encontrada")
        }
        bookingRepository.deleteById(id)
    }
}