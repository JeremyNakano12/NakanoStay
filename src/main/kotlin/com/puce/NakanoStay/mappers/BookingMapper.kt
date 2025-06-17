package com.puce.NakanoStay.mappers

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.models.responses.BookingDetailResponse
import com.puce.NakanoStay.models.responses.BookingResponse

fun Booking.toResponse(): BookingResponse =
    BookingResponse(
        id = this.id,
        userId = this.user.id,
        checkIn = this.checkIn,
        checkOut = this.checkOut,
        status = this.status,
        total = this.total,
        details = this.bookingDetails.map { it.toResponse() }
    )

fun BookingDetail.toResponse(): BookingDetailResponse =
    BookingDetailResponse(
        roomId = this.room.id,
        guests = this.guests,
        priceAtBooking = this.priceAtBooking
    )

fun BookingRequest.toEntity(user: User, rooms: List<Room>): Booking {
    val booking = Booking(
        user = user,
        checkIn = this.checkIn,
        checkOut = this.checkOut,
        status = this.status
    )

    val details = this.details.map { detail ->
        val room = rooms.first { it.id == detail.roomId }
        BookingDetail(
            booking = booking,
            room = room,
            guests = detail.guests,
            priceAtBooking = detail.priceAtBooking
        )
    }

    // Agregar los detalles a la colección mutable
    booking.bookingDetails.addAll(details)

    return booking
}