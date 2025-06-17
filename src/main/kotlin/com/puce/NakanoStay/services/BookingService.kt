package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.repositories.BookingDetailRepository
import com.puce.NakanoStay.repositories.BookingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val bookingDetailRepository: BookingDetailRepository
) {

    fun getAll(): List<Booking> = bookingRepository.findAll()

    fun getById(id: Long): Booking =
        bookingRepository.findById(id).orElseThrow {
            NotFoundException("Reserva con id $id no encontrada")
        }

    @Transactional
    fun save(booking: Booking): Booking {
        return bookingRepository.save(booking)
    }

    fun delete(id: Long) {
        if (!bookingRepository.existsById(id)) {
            throw NotFoundException("Reserva con id $id no encontrada")
        }
        bookingRepository.deleteById(id)
    }
}
