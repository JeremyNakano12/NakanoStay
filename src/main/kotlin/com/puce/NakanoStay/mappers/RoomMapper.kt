package com.puce.NakanoStay.mappers

import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.requests.RoomRequest
import com.puce.NakanoStay.models.responses.RoomResponse


fun RoomRequest.toEntity(hotel: Hotel): Room = Room(
        hotel = hotel,
        roomNumber = this.roomNumber,
        roomType = this.roomType,
        pricePerNight = this.pricePerNight,
        isAvailable = this.isAvailable
    )

fun Room.toResponse(): RoomResponse = RoomResponse(
        id = this.id,
        hotelId = this.hotel.id,
        roomNumber = this.roomNumber,
        roomType = this.roomType,
        pricePerNight = this.pricePerNight,
        isAvailable = this.isAvailable
    )

