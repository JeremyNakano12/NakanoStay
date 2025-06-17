package com.puce.NakanoStay.services

import com.puce.NakanoStay.exceptions.NotFoundException
import com.puce.NakanoStay.models.entities.User
import com.puce.NakanoStay.models.requests.UserRequest
import com.puce.NakanoStay.repositories.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(private val userRepository: UserRepository) {

    fun getAll(): List<User> = userRepository.findAll()

    fun getById(id: Long): User =
        userRepository.findById(id).orElseThrow {
            NotFoundException("Usuario con id $id no encontrado")
        }

    fun save(user: User): User = userRepository.save(user)

    fun delete(id: Long) {
        if (!userRepository.existsById(id)) {
            throw NotFoundException("Usuario con id $id no encontrado")
        }
        userRepository.deleteById(id)
    }

}


