package com.puce.NakanoStay.models.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.*

@Entity
@Table(name = "booking")
data class Booking(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "booking_date")
    val bookingDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "check_in", nullable = false)
    val checkIn: LocalDate,

    @Column(name = "check_out", nullable = false)
    val checkOut: LocalDate,

    @Column(nullable = false)
    val status: String = "pending",

    @OneToMany(
        mappedBy = "booking",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val bookingDetails: MutableList<BookingDetail> = mutableListOf()

) : BaseEntity() {
    val total: BigDecimal
        get() = bookingDetails.fold(BigDecimal.ZERO) { acc, detail -> acc + detail.priceAtBooking }
}