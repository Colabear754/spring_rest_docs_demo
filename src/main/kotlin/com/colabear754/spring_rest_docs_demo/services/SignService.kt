package com.colabear754.spring_rest_docs_demo.services

import com.colabear754.spring_rest_docs_demo.dto.SignInRequest
import com.colabear754.spring_rest_docs_demo.dto.SignInResponse
import com.colabear754.spring_rest_docs_demo.dto.SignUpRequest
import com.colabear754.spring_rest_docs_demo.dto.SignUpResponse
import com.colabear754.spring_rest_docs_demo.entity.Member
import com.colabear754.spring_rest_docs_demo.entity.MemberRefreshToken
import com.colabear754.spring_rest_docs_demo.repositories.MemberRefreshTokenRepository
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import com.colabear754.spring_rest_docs_demo.security.TokenProvider
import com.colabear754.spring_rest_docs_demo.util.flushOrThrow
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignService(
    private val memberRepository: MemberRepository,
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
    private val tokenProvider: TokenProvider,
    private val encoder: PasswordEncoder
) {
    @Transactional
    fun registMember(request: SignUpRequest) = SignUpResponse.from(
        memberRepository.flushOrThrow(IllegalArgumentException("이미 사용중인 아이디입니다.")) { save(Member.from(request, encoder)) }
    )

    @Transactional
    fun signIn(request: SignInRequest): SignInResponse {
        val member = memberRepository.findByAccount(request.account)
            ?.takeIf { encoder.matches(request.password, it.password) } ?: throw IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.")
        val accessToken = tokenProvider.createAccessToken("${member.id}:${member.type}")
        val refreshToken = tokenProvider.createRefreshToken()
        memberRefreshTokenRepository.findByIdOrNull(member.id)?.updateRefreshToken(refreshToken)
            ?: memberRefreshTokenRepository.save(MemberRefreshToken(member, refreshToken))
        return SignInResponse(member.name, member.type, accessToken, refreshToken)
    }
}