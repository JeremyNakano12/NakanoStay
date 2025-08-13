package com.puce.NakanoStay.mappers

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.entities.Room
import com.puce.NakanoStay.models.enums.BookingStatus
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.models.responses.BookingDetailResponse
import com.puce.NakanoStay.models.responses.BookingResponse

fun Booking.toResponse(): BookingResponse =
    BookingResponse(
        id = this.id,
        bookingCode = this.bookingCode,
        guestName = this.guestName,
        guestDni = this.guestDni,
        guestEmail = this.guestEmail,
        guestPhone = this.guestPhone,
        bookingDate = this.bookingDate,
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

fun BookingRequest.toEntity(bookingCode: String, rooms: List<Room>): Booking {
    val booking = Booking(
        bookingCode = bookingCode,
        guestName = this.guestName,
        guestDni = this.guestDni,
        guestEmail = this.guestEmail,
        guestPhone = this.guestPhone,
        checkIn = this.checkIn,
        checkOut = this.checkOut,
        status = BookingStatus.valueOf(this.status)
    )

    val numberOfNights = this.checkIn.until(this.checkOut).days.toLong()

    val details = this.details.map { detail ->
        val room = rooms.first { it.id == detail.roomId }
        BookingDetail(
            booking = booking,
            room = room,
            guests = detail.guests,
            priceAtBooking = room.pricePerNight.multiply(numberOfNights.toBigDecimal())
        )
    }

    booking.bookingDetails.addAll(details)
    return booking
}