package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.BookingDetail
import org.springframework.data.jpa.repository.JpaRepository

interface BookingDetailRepository : JpaRepository<BookingDetail, Long>
