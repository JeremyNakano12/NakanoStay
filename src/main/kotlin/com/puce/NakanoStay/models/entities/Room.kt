package com.puce.NakanoStay.models.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "rooms", uniqueConstraints = [UniqueConstraint(columnNames = ["hotel_id", "room_number"])])
data class Room(

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    val hotel: Hotel,

    @Column(name = "room_number", nullable = false, length = 10)
    val roomNumber: String,

    @Column(name = "room_type", length = 50)
    val roomType: String? = null,

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    val pricePerNight: BigDecimal,

    @Column(name = "is_available")
    val isAvailable: Boolean = true
): BaseEntity()
