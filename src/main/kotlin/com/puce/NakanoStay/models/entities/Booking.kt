package com.puce.NakanoStay.models.entities

import com.puce.NakanoStay.models.enums.BookingStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.*

@Entity
@Table(name = "booking")
data class Booking(

    @Column(name = "booking_code", unique = true, nullable = false, length = 16)
    val bookingCode: String,

    @Column(name = "guest_name", nullable = false, length = 100)
    val guestName: String,

    @Column(name = "guest_dni", nullable = false, length = 10)
    val guestDni: String,

    @Column(name = "guest_email", nullable = false, length = 100)
    val guestEmail: String,

    @Column(name = "guest_phone", length = 20)
    val guestPhone: String? = null,

    @Column(name = "booking_date")
    val bookingDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "check_in", nullable = false)
    val checkIn: LocalDate,

    @Column(name = "check_out", nullable = false)
    val checkOut: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: BookingStatus = BookingStatus.PENDING,

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