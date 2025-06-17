package com.puce.NakanoStay.controllers

import com.puce.NakanoStay.models.requests.UserRequest
import com.puce.NakanoStay.models.responses.*
import com.puce.NakanoStay.routes.Routes
import com.puce.NakanoStay.services.UserService
import com.puce.NakanoStay.mappers.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(Routes.BASE_URL + Routes.USERS)
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAll().map { it.toResponse() }
        return ResponseEntity.ok(users)
    }

    @GetMapping(Routes.ID)
    fun getById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getById(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @PostMapping
    fun create(@RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val user = request.toEntity()
        val savedUser = userService.save(user)
        return ResponseEntity.ok(savedUser.toResponse())
    }

    @DeleteMapping(Routes.DELETE + Routes.ID)
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }

}

