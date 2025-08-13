package com.puce.NakanoStay.services

import com.puce.NakanoStay.repositories.BookingRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Service
class BookingCodeService(
    private val bookingRepository: BookingRepository
) {

    companion object {
        private const val PREFIX = "NKS"
        private const val CODE_LENGTH = 6
        private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    fun generateUniqueBookingCode(): String {
        var attempts = 0
        val maxAttempts = 50

        while (attempts < maxAttempts) {
            val code = generateBookingCode()
            if (!bookingRepository.existsByBookingCode(code)) {
                return code
            }
            attempts++
        }

        throw RuntimeException("No se pudo generar un código único después de $maxAttempts intentos")
    }

    private fun generateBookingCode(): String {
        val suffix = (1..CODE_LENGTH)
            .map { CHARACTERS[Random.nextInt(CHARACTERS.length)] }
            .joinToString("")

        val now = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyMMdd")

        val dateSuffix = now.format(formatter)

        return "$PREFIX-$suffix$dateSuffix"
    }
}