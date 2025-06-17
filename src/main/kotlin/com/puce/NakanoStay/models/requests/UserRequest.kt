package com.puce.NakanoStay.models.requests

data class UserRequest(
    val name: String,
    val dni: String,
    val email: String,
    val phone: String? = null
)
