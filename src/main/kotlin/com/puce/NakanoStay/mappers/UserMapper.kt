package com.puce.NakanoStay.mappers

import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.models.requests.UserRequest
import com.puce.NakanoStay.models.responses.UserResponse
import org.springframework.stereotype.Component

fun UserRequest.toEntity(): User = User(
        name = this.name,
        dni = this.dni,
        email = this.email,
        phone = this.phone
    )

fun User.toResponse(): UserResponse = UserResponse(
        id = this.id,
        name = this.name,
        dni = this.dni,
        email = this.email,
        phone = this.phone
    )

