package com.puce.NakanoStay.repositories

import com.puce.NakanoStay.models.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>

