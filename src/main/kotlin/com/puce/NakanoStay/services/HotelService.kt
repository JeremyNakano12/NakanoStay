package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.repositories.HotelRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class HotelService(private val hotelRepository: HotelRepository) {

    fun getAll(): List<Hotel> = hotelRepository.findAll()

    fun getById(id: Long): Hotel =
        hotelRepository.findById(id).orElseThrow {
            NotFoundException("Hotel con id $id no encontrado")
        }

    fun save(hotel: Hotel): Hotel = hotelRepository.save(hotel)

    fun update(id: Long, hotel: Hotel): Hotel {
        if (!hotelRepository.existsById(id)) {
            throw NotFoundException("Hotel con id $id no encontrado")
        }
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
}
