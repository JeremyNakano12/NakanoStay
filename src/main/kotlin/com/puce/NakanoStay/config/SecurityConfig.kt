package com.puce.NakanoStay.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val supabaseJwtFilter: SupabaseJwtFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoints públicos
                    .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bookings/code/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/bookings/code/*/cancel").permitAll()

                    // Todo los demás requieren admin
                    .anyRequest().hasRole("ADMIN")
            }
            .addFilterBefore(supabaseJwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}

@Component
class SupabaseJwtFilter(
    @Value("\${supabase.jwt.secret}") private val jwtSecret: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)

            if (token != null && isValidToken(token)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    "ADMIN", // Subject del token
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            logger.debug("Error processing JWT token: ${ex.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        return if (authHeader?.startsWith("Bearer ") == true) {
            authHeader.substring(7)
        } else null
    }

    private fun isValidToken(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false

            val header = String(Base64.getUrlDecoder().decode(parts[0]))
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))

            header.contains("\"typ\":\"JWT\"") && payload.contains("\"sub\":")
        } catch (ex: Exception) {
            false
        }
    }

    private fun verifySignature(token: String): Boolean {
        return try {
            val parts = token.split(".")
            val headerAndPayload = "${parts[0]}.${parts[1]}"
            val signature = parts[2]

            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
            mac.init(secretKey)

            val computedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(headerAndPayload.toByteArray()))

            signature == computedSignature
        } catch (ex: Exception) {
            false
        }
    }
}