package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.models.entities.Booking
import com.puce.NakanoStay.models.entities.BookingDetail
import com.puce.NakanoStay.models.requests.BookingRequest
import com.puce.NakanoStay.models.responses.BookingResponse
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.BookingCodeService
import com.puce.NakanoStay.services.BookingService
import com.puce.NakanoStay.services.EmailService
import com.puce.NakanoStay.services.RoomService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.puce.NakanoStay.mappers.*

@RestController
@RequestMapping(Routes.BASE_URL + Routes.BOOKINGS)
class BookingController(
    private val bookingService: BookingService,
    private val roomService: RoomService,
    private val emailService: EmailService,
    private val bookingCodeService: BookingCodeService
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

    @GetMapping("/code/{code}")
    fun getByCode(
        @PathVariable code: String,
        @RequestParam dni: String
    ): ResponseEntity<BookingResponse> {
        val booking = bookingService.getByCodeAndDni(code, dni)
        return ResponseEntity.ok(booking.toResponse())
    }

    @PostMapping
    fun create(@Valid @RequestBody request: BookingRequest): ResponseEntity<BookingResponse> {
        val bookingCode = bookingCodeService.generateUniqueBookingCode()

        val rooms = request.details.map { detail ->
            roomService.getById(detail.roomId)
        }

        val booking = request.toEntity(bookingCode, rooms)

        val savedBooking = bookingService.save(booking)

        emailService.sendBookingConfirmation(savedBooking)

        return ResponseEntity.ok(savedBooking.toResponse())
    }

    @PutMapping("/code/{code}/cancel")
    fun cancelBooking(
        @PathVariable code: String,
        @RequestParam dni: String
    ): ResponseEntity<BookingResponse> {
        val cancelledBooking = bookingService.cancelBooking(code, dni)

        emailService.sendCancellationNotification(cancelledBooking)

        return ResponseEntity.ok(cancelledBooking.toResponse())
    }

    @PutMapping("/code/{code}/confirm")
    fun confirmBooking(
        @PathVariable code: String,
        @RequestParam dni: String
    ): ResponseEntity<BookingResponse> {
        val confirmedBooking = bookingService.confirmBooking(code, dni)

        emailService.sendBookingLastConfirmation(confirmedBooking)

        return ResponseEntity.ok(confirmedBooking.toResponse())
    }

    @PutMapping("/code/{code}/complete")
    fun completeBooking(
        @PathVariable code: String,
        @RequestParam dni: String
    ): ResponseEntity<BookingResponse> {
        val completedBooking = bookingService.completeBooking(code, dni)

        emailService.sendBookingCompletedNotification(completedBooking)

        return ResponseEntity.ok(completedBooking.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        bookingService.delete(id)
        return ResponseEntity.noContent().build()
    }
}