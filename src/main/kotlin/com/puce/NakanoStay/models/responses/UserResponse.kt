package com.puce.NakanoStay.models.responses


data class UserResponse(
    val id: Long,
    val name: String,
    val dni: String,
    val email: String,
    val phone: String?
)
