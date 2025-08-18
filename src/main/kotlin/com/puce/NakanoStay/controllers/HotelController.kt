package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.models.requests.HotelRequest
import com.puce.NakanoStay.models.responses.HotelResponse
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.HotelService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.puce.NakanoStay.mappers.*

@RestController
@RequestMapping(Routes.BASE_URL + Routes.HOTELS)
class HotelController(
    private val hotelService: HotelService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<HotelResponse>> {
        val hotels = hotelService.getAll().map { it.toResponse() }
        return ResponseEntity.ok(hotels)
    }

    @GetMapping(Routes.ID)
    fun getById(@PathVariable id: Long): ResponseEntity<HotelResponse> {
        val hotel = hotelService.getById(id)
        return ResponseEntity.ok(hotel.toResponse())
    }

    @PostMapping
    fun create(@RequestBody request: HotelRequest): ResponseEntity<HotelResponse> {
        val hotel = request.toEntity()
        val savedHotel = hotelService.save(hotel)
        return ResponseEntity.ok(savedHotel.toResponse())
    }

    @PutMapping(Routes.ID)
    fun update(@PathVariable id: Long, @RequestBody request: HotelRequest): ResponseEntity<HotelResponse> {
        val hotel = request.toEntity()
        val updatedHotel = hotelService.update(id, hotel)
        return ResponseEntity.ok(updatedHotel.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        hotelService.delete(id)
        return ResponseEntity.noContent().build()
    }
}