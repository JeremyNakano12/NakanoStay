package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.repositories.RoomRepository
import org.springframework.stereotype.Service

import java.util.*

@Service
class RoomService(private val roomRepository: RoomRepository) {

    fun getAll(): List<Room> = roomRepository.findAll()

    fun getByHotel(hotelId: Long): List<Room> = roomRepository.findByHotelId(hotelId)

    fun getById(id: Long): Room =
        roomRepository.findById(id).orElseThrow {
            NotFoundException("Habitación con id $id no encontrada")
        }

    fun save(room: Room): Room = roomRepository.save(room)

    fun update(id: Long, room: Room): Room {
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }
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

    fun delete(id: Long) {
        if (!roomRepository.existsById(id)) {
            throw NotFoundException("Habitación con id $id no encontrada")
        }
        roomRepository.deleteById(id)
    }
}