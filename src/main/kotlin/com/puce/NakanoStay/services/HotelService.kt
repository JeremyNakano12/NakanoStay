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

    fun delete(id: Long) {
        if (!hotelRepository.existsById(id)) {
            throw NotFoundException("Hotel con id $id no encontrado")
        }
        hotelRepository.deleteById(id)
    }
}
