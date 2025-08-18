package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long> {
    fun findByHotelId(hotelId: Long): List<Room>
    fun existsByHotelIdAndRoomNumber(hotelId: Long, roomNumber: String): Boolean
    fun existsByHotelIdAndRoomNumberAndIdNot(hotelId: Long, roomNumber: String, id: Long): Boolean
}