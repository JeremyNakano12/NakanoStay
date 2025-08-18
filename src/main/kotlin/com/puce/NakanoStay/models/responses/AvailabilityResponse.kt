package com.puce.NakanoStay.models.responses

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AvailabilityResponse(
    val roomId: Long,
    val availableDates: List<LocalDate>,
    val occupiedRanges: List<DateRange>
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)