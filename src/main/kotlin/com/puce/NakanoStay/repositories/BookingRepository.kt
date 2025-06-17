package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Booking
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository : JpaRepository<Booking, Long>

