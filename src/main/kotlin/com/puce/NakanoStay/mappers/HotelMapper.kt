package com.puce.NakanoStay.mappers

import com.puce.NakanoStay.models.entities.Hotel
import com.puce.NakanoStay.models.requests.HotelRequest
import com.puce.NakanoStay.models.responses.HotelResponse

fun HotelRequest.toEntity(): Hotel = Hotel(
        name = this.name,
        address = this.address,
        city = this.city,
        stars = this.stars
    )

fun Hotel.toResponse(): HotelResponse = HotelResponse(
        id = this.id,
        name = this.name,
        address = this.address,
        city = this.city,
        stars = this.stars,
    )

