package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.mappers.*
import com.puce.NakanoStay.models.requests.RoomRequest
import com.puce.NakanoStay.models.responses.AvailabilityResponse
import com.puce.NakanoStay.models.responses.BookingResponse
import com.puce.NakanoStay.models.responses.RoomResponse
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import com.puce.NakanoStay.services.RoomService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping(Routes.BASE_URL + Routes.ROOM)
class RoomController(
    private val roomService: RoomService,
    private val hotelService: HotelService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getAll().map { it.toResponse() }
        return ResponseEntity.ok(rooms)
    }

    @GetMapping(Routes.HOTEL_ID)
    fun getByHotel(@PathVariable hotelId: Long): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getByHotel(hotelId).map { it.toResponse() }
        return ResponseEntity.ok(rooms)
    }

    @GetMapping("/{id}/availability")
    fun getRoomAvailability(
        @PathVariable id: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<AvailabilityResponse> {
        val availability = roomService.getAvailability(id, startDate, endDate)
        return ResponseEntity.ok(availability)
    }

    @PostMapping
    fun create(@RequestBody request: RoomRequest): ResponseEntity<RoomResponse> {
        val hotel = hotelService.getById(request.hotelId)
        val room = request.toEntity(hotel)
        val savedRoom = roomService.save(room)
        return ResponseEntity.ok(savedRoom.toResponse())
    }

    @PutMapping(Routes.ID)
    fun update(@PathVariable id: Long, @RequestBody request: RoomRequest): ResponseEntity<RoomResponse> {
        val hotel = hotelService.getById(request.hotelId)
        val room = request.toEntity(hotel)
        val updatedRoom = roomService.update(id, room)
        return ResponseEntity.ok(updatedRoom.toResponse())
    }

    @PutMapping("/{id}/available")
    fun available(@PathVariable id: Long): ResponseEntity<RoomResponse> {
        val availableRoom = roomService.availableRoom(id)
        return ResponseEntity.ok(availableRoom.toResponse())
    }

    @PutMapping("/{id}/unavailable")
    fun unavailable(@PathVariable id: Long): ResponseEntity<RoomResponse> {
        val unavailableRoom = roomService.unavailableRoom(id)
        return ResponseEntity.ok(unavailableRoom.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        roomService.delete(id)
        return ResponseEntity.noContent().build()
    }
}