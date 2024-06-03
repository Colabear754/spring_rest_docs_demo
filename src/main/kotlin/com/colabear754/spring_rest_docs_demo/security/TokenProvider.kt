package com.colabear754.spring_rest_docs_demo.security

import com.colabear754.spring_rest_docs_demo.repositories.MemberRefreshTokenRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec

@PropertySource("classpath:jwt.yml")
@Service
class TokenProvider(
    @Value("\${secret-key}")
    private val secretKey: String,
    @Value("\${expiration-minutes}")
    private val expirationMinutes: Long,
    @Value("\${refresh-expiration-hours}")
    private val refreshExpirationHours: Long,
    @Value("\${issuer}")
    private val issuer: String,
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository
) {
    private val reissueLimit = refreshExpirationHours * 60 / expirationMinutes
    private val objectMapper = ObjectMapper()

    fun createAccessToken(userSpecification: String) = Jwts.builder()
        .signWith(SecretKeySpec(secretKey.toByteArray(), SignatureAlgorithm.HS512.jcaName))
        .setSubject(userSpecification)
        .setIssuer(issuer)
        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
        .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
        .compact()!!

    fun createRefreshToken() = Jwts.builder()
        .signWith(SecretKeySpec(secretKey.toByteArray(), SignatureAlgorithm.HS512.jcaName))
        .setIssuer(issuer)
        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
        .setExpiration(Date.from(Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS)))
        .compact()!!

    fun validateTokenAndGetSubject(token: String?): String? = validateAndParseToken(token)
        .body
        .subject

    @Transactional
    fun recreateAccessToken(oldAccessToken: String): String {
        val subject = decodeJwtPayloadSubject(oldAccessToken)
        memberRefreshTokenRepository.findByMemberIdAndReissueCountLessThan(UUID.fromString(subject.split(':')[0]), reissueLimit)
            ?.increaseReissueCount() ?: throw ExpiredJwtException(null, null, "Refresh token is expired.")
        return createAccessToken(subject)
    }

    @Transactional(readOnly = true)
    fun validateRefreshToken(refreshToken: String, oldAccessToken: String) {
        validateAndParseToken(refreshToken)
        val memberId = decodeJwtPayloadSubject(oldAccessToken).split(':')[0]
        memberRefreshTokenRepository.findByMemberIdAndReissueCountLessThan(UUID.fromString(memberId), reissueLimit)
            ?.takeIf { it.validateRefreshToken(refreshToken) } ?: throw ExpiredJwtException(null, null, "Refresh token is expired.")
    }

    private fun validateAndParseToken(token: String?) = Jwts.parserBuilder()
        .setSigningKey(secretKey.toByteArray())
        .build()
        .parseClaimsJws(token)!!

    private fun decodeJwtPayloadSubject(oldAccessToken: String) =
        objectMapper.readValue(
            Base64.getUrlDecoder().decode(oldAccessToken.split('.')[1]).decodeToString(),
            Map::class.java
        )["sub"].toString()
}