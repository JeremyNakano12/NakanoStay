package com.puce.NakanoStay.models.requests

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class BookingRequest(
    @field:NotBlank(message = "Nombre del huésped es requerido")
    @field:Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    val guestName: String,

    @field:Pattern(regexp = "^[0-9]{10}$", message = "DNI debe tener exactamente 10 dígitos")
    val guestDni: String,

    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "Email es requerido")
    val guestEmail: String,

    val guestPhone: String? = null,

    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: String = "PENDING",

    @field:Valid
    val details: List<BookingDetailRequest>
)