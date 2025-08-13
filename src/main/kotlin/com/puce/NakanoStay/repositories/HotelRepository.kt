package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.Hotel
import org.springframework.data.jpa.repository.JpaRepository

interface HotelRepository : JpaRepository<Hotel, Long> {
    fun existsByNameAndAddress(name: String, address: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
    fun existsByNameAndAddressAndIdNot(name: String, address: String, id: Long): Boolean
}