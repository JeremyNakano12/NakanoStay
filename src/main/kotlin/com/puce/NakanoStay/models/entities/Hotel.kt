package com.puce.NakanoStay.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "hotels")
data class Hotel(

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val address: String,

    @Column(length = 100)
    val city: String? = null,

    @Column
    val stars: Int? = null,

    @Column(nullable = false, unique = true, length = 100)
    val email: String

): BaseEntity()