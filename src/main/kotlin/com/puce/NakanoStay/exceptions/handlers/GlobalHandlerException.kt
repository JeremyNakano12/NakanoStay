package com.puce.NakanoStay.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.NOT_FOUND)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(HandleUnauthorizedException::class)
    fun handleUnauthorized(ex: HandleUnauthorizedException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.UNAUTHORIZED)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to "Unexpected error: ${ex.message}"), HttpStatus.INTERNAL_SERVER_ERROR)
}