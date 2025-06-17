package com.puce.NakanoStay.models.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "booking_details")
data class BookingDetail(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    val booking: Booking, // Cambiar de 'var' a 'val'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: Room,

    @Column(nullable = false)
    val guests: Int,

    @Column(name = "price_at_booking", nullable = false, precision = 10, scale = 2)
    val priceAtBooking: BigDecimal
) : BaseEntity()