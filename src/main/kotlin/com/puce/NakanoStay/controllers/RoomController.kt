package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.mappers.*
import com.puce.NakanoStay.models.requests.RoomRequest
import com.puce.NakanoStay.models.responses.RoomResponse
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import com.puce.NakanoStay.services.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PostMapping
    fun create(@RequestBody request: RoomRequest): ResponseEntity<RoomResponse> {
        val hotel = hotelService.getById(request.hotelId)
        val room = request.toEntity(hotel)
        val savedRoom = roomService.save(room)
        return ResponseEntity.ok(savedRoom.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        roomService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

