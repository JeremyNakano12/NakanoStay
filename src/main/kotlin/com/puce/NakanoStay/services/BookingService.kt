package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.repositories.BookingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
        validateBooking(booking)
        validateBookingDates(booking)
        validateRoomAvailability(booking)
        return bookingRepository.save(booking)
    }

    @Transactional
    fun cancelBooking(bookingCode: String, guestDni: String): Booking {
        val booking = getByCodeAndDni(bookingCode, guestDni)

        if (booking.status == BookingStatus.CANCELLED) {
            throw ConflictException("La reserva ya está cancelada")
        }

        if (booking.status == BookingStatus.COMPLETED) {
            throw ConflictException("No se puede cancelar una reserva completada")
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

    @Transactional
    fun confirmBooking(bookingCode: String, guestDni: String): Booking {
        val booking = getByCodeAndDni(bookingCode, guestDni)

        if (booking.status == BookingStatus.CONFIRMED) {
            throw ConflictException("La reserva ya está confirmada")
        }

        if (booking.status == BookingStatus.CANCELLED) {
            throw ConflictException("No se puede confirmar una reserva cancelada")
        }

        if (booking.status == BookingStatus.COMPLETED) {
            throw ConflictException("No se puede confirmar una reserva completada")
        }

        val confirmedBooking = Booking(
            bookingCode = booking.bookingCode,
            guestName = booking.guestName,
            guestDni = booking.guestDni,
            guestEmail = booking.guestEmail,
            guestPhone = booking.guestPhone,
            bookingDate = booking.bookingDate,
            checkIn = booking.checkIn,
            checkOut = booking.checkOut,
            status = BookingStatus.CONFIRMED
        )

        confirmedBooking.id = booking.id
        confirmedBooking.bookingDetails.addAll(booking.bookingDetails)

        return bookingRepository.save(confirmedBooking)
    }

    @Transactional
    fun completeBooking(bookingCode: String, guestDni: String): Booking {
        val booking = getByCodeAndDni(bookingCode, guestDni)

        if (booking.status == BookingStatus.COMPLETED) {
            throw ConflictException("La reserva ya está completada")
        }

        if (booking.status == BookingStatus.PENDING) {
            throw ConflictException("No se puede completar una reserva no confirmada")
        }

        if (booking.status == BookingStatus.CANCELLED) {
            throw ConflictException("No se puede completar una reserva cancelada")
        }

        val completedBooking = Booking(
            bookingCode = booking.bookingCode,
            guestName = booking.guestName,
            guestDni = booking.guestDni,
            guestEmail = booking.guestEmail,
            guestPhone = booking.guestPhone,
            bookingDate = booking.bookingDate,
            checkIn = booking.checkIn,
            checkOut = booking.checkOut,
            status = BookingStatus.COMPLETED
        )

        completedBooking.id = booking.id
        completedBooking.bookingDetails.addAll(booking.bookingDetails)

        return bookingRepository.save(completedBooking)
    }

    fun delete(id: Long) {
        if (!bookingRepository.existsById(id)) {
            throw NotFoundException("Reserva con id $id no encontrada")
        }
        bookingRepository.deleteById(id)
    }

    private fun validateBooking(booking: Booking) {

        if (booking.bookingCode.isBlank()) {
            throw ValidationException("El código de reserva es requerido")
        }
        if (booking.bookingCode.length > 16) {
            throw ValidationException("El código de reserva no puede tener más de 16 caracteres")
        }
        if (booking.guestName.isBlank()) {
            throw ValidationException("El nombre del huésped es requerido")
        }
        if (booking.guestName.length < 2) {
            throw ValidationException("El nombre del huésped debe tener al menos 2 caracteres")
        }
        if (booking.guestName.length > 100) {
            throw ValidationException("El nombre del huésped no puede tener más de 100 caracteres")
        }
        if (booking.guestDni.isBlank()) {
            throw ValidationException("El DNI del huésped es requerido")
        }
        if (!validateDni(booking.guestDni)) {
            throw ValidationException("La cédula debe ser valida")
        }
        if (booking.guestEmail.isBlank()) {
            throw ValidationException("El email del huésped es requerido")
        }
        if (!isValidEmail(booking.guestEmail)) {
            throw ValidationException("El formato del email es inválido")
        }
        if (booking.guestEmail.length > 100) {
            throw ValidationException("El email no puede tener más de 100 caracteres")
        }

        booking.guestPhone?.let { phone ->
            if (phone.isNotBlank()) {
                if (phone.length < 9 || phone.length > 15) {
                    throw ValidationException("El teléfono debe ser valido")
                }

                if (!phone.matches("^[+0-9\\s-]+$".toRegex())) {
                    throw ValidationException("El teléfono debe ser valido")
                }

                val digitsOnly = phone.replace("[^0-9]".toRegex(), "")
                if (digitsOnly.length < 9) {
                    throw ValidationException("El teléfono debe contener al menos 9 dígitos")
                }
            }
        }

        if (booking.bookingDetails.isEmpty()) {
            throw ValidationException("La reserva debe tener al menos una habitación")
        }

        booking.bookingDetails.forEach { detail ->
            validateBookingDetail(detail)
        }
    }

    private fun validateBookingDetail(detail: BookingDetail) {
        if (detail.guests <= 0) {
            throw ValidationException("El número de huéspedes debe ser mayor a 0")
        }
        if (detail.guests > 10) {
            throw ValidationException("El número de huéspedes no puede ser mayor a 10 por habitación")
        }
    }

    private fun validateBookingDates(booking: Booking) {
        val today = LocalDate.now()

        if (booking.checkIn.isBefore(today)) {
            throw ValidationException("La fecha de check-in no puede ser en el pasado")
        }

        if (booking.checkOut.isBefore(booking.checkIn) || booking.checkOut.isEqual(booking.checkIn)) {
            throw ValidationException("La fecha de check-out debe ser posterior a la fecha de check-in")
        }

        val daysBetween = ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut)
        if (daysBetween > 30) {
            throw ValidationException("La estadía no puede ser mayor a 30 días")
        }
    }

    private fun validateRoomAvailability(booking: Booking) {
        val roomIds = booking.bookingDetails.map { it.room.id }

        val hasConflict = bookingRepository.existsConflictingBooking(
            roomIds = roomIds,
            checkIn = booking.checkIn,
            checkOut = booking.checkOut,
            excludedStatuses = listOf(BookingStatus.CANCELLED)
        )

        if (hasConflict) {
            throw ConflictException(
                "Una o más habitaciones no están disponibles para las fechas seleccionadas. " +
                        "Ya existe una reserva que se solapa con el período ${booking.checkIn} - ${booking.checkOut}"
            )
        }

        booking.bookingDetails.forEach { detail ->
            if (!detail.room.isAvailable) {
                throw ConflictException("La habitación ${detail.room.roomNumber} no está disponible")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
        return email.matches(emailRegex.toRegex())
    }

    fun validateDni(dni: String): Boolean {
        if (!dni.matches("^[0-9]{10}$".toRegex())) {
            return false
        }

        val provinceCode = dni.substring(0, 2).toInt()
        if (provinceCode < 1 || provinceCode > 24 && provinceCode != 30) {
            return false
        }

        val digits = dni.toCharArray().map { it.toString().toInt() }
        val verifierDigit = digits.last()
        val checkSum = digits.dropLast(1)
            .mapIndexed { index, digit ->
                if (index % 2 == 0) {
                    var calculated = digit * 2
                    if (calculated > 9) {
                        calculated -= 9
                    }
                    calculated
                } else {
                    digit
                }
            }.sum()

        val calculatedVerifier = if (checkSum % 10 == 0) 0 else 10 - (checkSum % 10)

        return verifierDigit == calculatedVerifier
    }
}