package com.puce.NakanoStay.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, unique = true, length = 10)
    val dni: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(length = 20)
    val phone: String? = null,



): BaseEntity()