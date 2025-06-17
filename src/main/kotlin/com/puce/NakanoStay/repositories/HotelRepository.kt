package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Hotel
import org.springframework.data.jpa.repository.JpaRepository

interface HotelRepository : JpaRepository<Hotel, Long>

