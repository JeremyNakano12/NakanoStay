package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.ConflictException
import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.exceptions.ValidationException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.repositories.HotelRepository
import org.springframework.stereotype.Service

@Service
class HotelService(private val hotelRepository: HotelRepository) {

    fun getAll(): List<Hotel> = hotelRepository.findAll()

    fun getById(id: Long): Hotel =
        hotelRepository.findById(id).orElseThrow {
            NotFoundException("Hotel con id $id no encontrado")
        }

    fun save(hotel: Hotel): Hotel {
        validateHotel(hotel)
        validateUniqueConstraints(hotel)
        return hotelRepository.save(hotel)
    }

    fun update(id: Long, hotel: Hotel): Hotel {
        if (!hotelRepository.existsById(id)) {
            throw NotFoundException("Hotel con id $id no encontrado")
        }

        validateHotel(hotel)
        validateUniqueConstraintsForUpdate(hotel, id)

        val updatedHotel = Hotel(
            name = hotel.name,
            address = hotel.address,
            city = hotel.city,
            stars = hotel.stars,
            email = hotel.email
        )
        updatedHotel.id = id
        return hotelRepository.save(updatedHotel)
    }

    fun delete(id: Long) {
        if (!hotelRepository.existsById(id)) {
            throw NotFoundException("Hotel con id $id no encontrado")
        }
        hotelRepository.deleteById(id)
    }

    private fun validateHotel(hotel: Hotel) {
        if (hotel.name.isBlank()) {
            throw ValidationException("El nombre del hotel es requerido")
        }
        if (hotel.name.length > 100) {
            throw ValidationException("El nombre del hotel no puede tener más de 100 caracteres")
        }

        if (hotel.address.isBlank()) {
            throw ValidationException("La dirección del hotel es requerida")
        }

        if (hotel.email.isBlank()) {
            throw ValidationException("El email del hotel es requerido")
        }
        if (!isValidEmail(hotel.email)) {
            throw ValidationException("El formato del email es inválido")
        }
        if (hotel.email.length > 100) {
            throw ValidationException("El email no puede tener más de 100 caracteres")
        }

        hotel.city?.let { city ->
            if (city.length > 100) {
                throw ValidationException("La ciudad no puede tener más de 100 caracteres")
            }
        }

        hotel.stars?.let { stars ->
            if (stars < 0) {
                throw ValidationException("Las estrellas del hotel no pueden ser negativas")
            }
            if (stars > 5) {
                throw ValidationException("Las estrellas del hotel no pueden ser más de 5")
            }
        }
    }

    private fun validateUniqueConstraints(hotel: Hotel) {
        if (hotelRepository.existsByNameAndAddress(hotel.name, hotel.address)) {
            throw ConflictException("Ya existe un hotel con el nombre '${hotel.name}' en la dirección '${hotel.address}'")
        }

        if (hotelRepository.existsByEmail(hotel.email)) {
            throw ConflictException("Ya existe un hotel registrado con el email '${hotel.email}'")
        }
    }

    private fun validateUniqueConstraintsForUpdate(hotel: Hotel, id: Long) {
        if (hotelRepository.existsByNameAndAddressAndIdNot(hotel.name, hotel.address, id)) {
            throw ConflictException("Ya existe otro hotel con el nombre '${hotel.name}' en la dirección '${hotel.address}'")
        }

        if (hotelRepository.existsByEmailAndIdNot(hotel.email, id)) {
            throw ConflictException("Ya existe otro hotel registrado con el email '${hotel.email}'")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
        return email.matches(emailRegex.toRegex())
    }
}