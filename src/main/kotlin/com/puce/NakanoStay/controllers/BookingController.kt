package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.models.responses.BookingResponse
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.BookingService
import com.puce.NakanoStay.services.RoomService
import com.puce.NakanoStay.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.puce.NakanoStay.mappers.*

@RestController
@RequestMapping(Routes.BASE_URL + Routes.BOOKINGS)
class BookingController(
    private val bookingService: BookingService,
    private val userService: UserService,
    private val roomService: RoomService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getAll().map { it.toResponse() }
        return ResponseEntity.ok(bookings)
    }

    @GetMapping(Routes.ID)
    fun getById(@PathVariable id: Long): ResponseEntity<BookingResponse> {
        val booking = bookingService.getById(id)
        return ResponseEntity.ok(booking.toResponse())
    }

    @PostMapping
    fun create(@RequestBody request: BookingRequest): ResponseEntity<BookingResponse> {
        val user = userService.getById(request.userId)

        // Crea el booking
        val booking = Booking(
            user = user,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            status = request.status
        )

        // Guarda primero el booking para obtener el ID
        val savedBooking = bookingService.save(booking)

        // Crea los detalles con el booking ya guardado
        val details = request.details.map { detailReq ->
            val room = roomService.getById(detailReq.roomId)
            BookingDetail(
                booking = savedBooking,
                room = room,
                guests = detailReq.guests,
                priceAtBooking = detailReq.priceAtBooking
            )
        }

        // Agrega los detalles a la colección
        savedBooking.bookingDetails.addAll(details)

        // Guarda nuevamente para persistir los detalles
        val finalBooking = bookingService.save(savedBooking)
        return ResponseEntity.ok(finalBooking.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        bookingService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
