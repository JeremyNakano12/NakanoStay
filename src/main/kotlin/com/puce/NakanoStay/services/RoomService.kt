package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.models.responses.AvailabilityResponse
import com.puce.NakanoStay.models.responses.DateRange
import com.puce.NakanoStay.repositories.BookingRepository
import com.puce.NakanoStay.repositories.RoomRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val bookingRepository: BookingRepository
) {

    fun getAll(): List<Room> = roomRepository.findAll()

    fun getByHotel(hotelId: Long): List<Room> = roomRepository.findByHotelId(hotelId)

    fun getById(id: Long): Room =
        roomRepository.findById(id).orElseThrow {
            NotFoundException("Habitación con id $id no encontrada")
        }

    fun getAvailability(roomId: Long, startDate: LocalDate, endDate: LocalDate): AvailabilityResponse {
        val room = getById(roomId)

        if (startDate.isAfter(endDate)) {
            throw ValidationException("La fecha de inicio debe ser anterior a la fecha de fin")
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw ValidationException("La fecha de inicio no puede ser en el pasado")
        }

        val bookings = bookingRepository.findAll()
            .filter { booking ->
                booking.bookingDetails.any { detail -> detail.room.id == roomId } &&
                        booking.status != BookingStatus.CANCELLED &&
                        !(booking.checkOut.isBefore(startDate) || booking.checkIn.isAfter(endDate))
            }

        val occupiedRanges = bookings.map { booking ->
            DateRange(
                start = maxOf(booking.checkIn, startDate),
                end = minOf(booking.checkOut.minusDays(1), endDate) // checkOut no incluye el día
            )
        }.sortedBy { it.start }

        val allDates = generateSequence(startDate) { date ->
            if (date.isBefore(endDate)) date.plusDays(1) else null
        }.toList()

        val availableDates = allDates.filter { date ->
            occupiedRanges.none { range ->
                !date.isBefore(range.start) && !date.isAfter(range.end)
            }
        }

        return AvailabilityResponse(
            roomId = roomId,
            availableDates = availableDates,
            occupiedRanges = occupiedRanges
        )
    }

    fun save(room: Room): Room {
        validateRoom(room)
        validateUniqueConstraints(room)
        return roomRepository.save(room)
    }

    fun update(id: Long, room: Room): Room {
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }

        validateRoom(room)
        validateUniqueConstraintsForUpdate(room, id)

        val updatedRoom = Room(
            hotel = room.hotel,
            roomNumber = room.roomNumber,
            roomType = room.roomType,
            pricePerNight = room.pricePerNight,
            isAvailable = room.isAvailable
        )
        updatedRoom.id = id
        return roomRepository.save(updatedRoom)
    }

    fun unavailableRoom(id: Long): Room {
        val room = getById(id)
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }

        if (!room.isAvailable) {
            throw ConflictException("La habitación no está disponible")
        }

        validateRoom(room)
        validateUniqueConstraintsForUpdate(room, id)

        val updatedRoom = Room(
            hotel = room.hotel,
            roomNumber = room.roomNumber,
            roomType = room.roomType,
            pricePerNight = room.pricePerNight,
            isAvailable = false
        )
        updatedRoom.id = id
        return roomRepository.save(updatedRoom)
    }

    fun availableRoom(id: Long): Room {
        val room = getById(id)
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }

        if (room.isAvailable) {
            throw ConflictException("La habitación ya está disponible")
        }

        validateRoom(room)
        validateUniqueConstraintsForUpdate(room, id)

        val availableRoom = Room(
            hotel = room.hotel,
            roomNumber = room.roomNumber,
            roomType = room.roomType,
            pricePerNight = room.pricePerNight,
            isAvailable = true
        )
        availableRoom.id = id
        return roomRepository.save(availableRoom)
    }

    fun delete(id: Long) {
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }
        roomRepository.deleteById(id)
    }

    private fun validateRoom(room: Room) {
        if (room.roomNumber.isBlank()) {
            throw ValidationException("El número de habitación es requerido")
        }
        if (room.roomNumber.length > 10) {
            throw ValidationException("El número de habitación no puede tener más de 10 caracteres")
        }

        room.roomType?.let { roomType ->
            if (roomType.isBlank()) {
                throw ValidationException("El tipo de habitación no puede estar vacío")
            }
            if (roomType.length > 50) {
                throw ValidationException("El tipo de habitación no puede tener más de 50 caracteres")
            }
        }

        if (room.pricePerNight < BigDecimal.ZERO) {
            throw ValidationException("El precio por noche no puede ser negativo")
        }
        if (room.pricePerNight.scale() > 2) {
            throw ValidationException("El precio por noche no puede tener más de 2 decimales")
        }
        if (room.pricePerNight.precision() > 10) {
            throw ValidationException("El precio por noche es demasiado grande")
        }

        if (room.hotel.id == 0L) {
            throw ValidationException("El hotel asociado a la habitación es requerido")
        }
    }

    private fun validateUniqueConstraints(room: Room) {
        if (roomRepository.existsByHotelIdAndRoomNumber(room.hotel.id, room.roomNumber)) {
            throw ConflictException("Ya existe una habitación con el número '${room.roomNumber}' en este hotel")
        }
    }

    private fun validateUniqueConstraintsForUpdate(room: Room, id: Long) {
        if (roomRepository.existsByHotelIdAndRoomNumberAndIdNot(room.hotel.id, room.roomNumber, id)) {
            throw ConflictException("Ya existe otra habitación con el número '${room.roomNumber}' en este hotel")
        }
    }
}