package com.colabear754.spring_rest_docs_demo.security

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(0)
@Component
class JwtAuthenticationFilter(private val tokenProvider: TokenProvider) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val accessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION)
            val user = parseUserSpecification(accessToken)
            UsernamePasswordAuthenticationToken.authenticated(user, accessToken, user.authorities)
                .apply { details = WebAuthenticationDetails(request) }
                .also { SecurityContextHolder.getContext().authentication = it }
        } catch (e: ExpiredJwtException) {
            reissueAccessToken(request, response, e)
        } catch (e: Exception) {
            request.setAttribute("exception", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun parseBearerToken(request: HttpServletRequest, headerName: String) = request.getHeader(headerName)
        .takeIf { it?.startsWith("Bearer ", true) ?: false }?.substring(7)

    private fun parseUserSpecification(token: String?) = (
            token?.takeIf { it.length >= 10 }
                ?.let { tokenProvider.validateTokenAndGetSubject(it) }
                ?: "anonymous:anonymous"
            ).split(":")
        .let { User(it[0], "", listOf(SimpleGrantedAuthority(it[1]))) }

    private fun reissueAccessToken(request: HttpServletRequest, response: HttpServletResponse, exception: Exception) {
        try {
            val refreshToken = parseBearerToken(request, "Refresh-Token") ?: throw exception
            val oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION)!!
            tokenProvider.validateRefreshToken(refreshToken, oldAccessToken)
            val newAccessToken = tokenProvider.recreateAccessToken(oldAccessToken)
            val user = parseUserSpecification(newAccessToken)
            UsernamePasswordAuthenticationToken.authenticated(user, newAccessToken, user.authorities)
                .apply { details = WebAuthenticationDetails(request) }
                .also { SecurityContextHolder.getContext().authentication = it }
            response.setHeader("New-Access-Token", newAccessToken)
        } catch (e: Exception) {
            request.setAttribute("exception", e)
        }
    }
}