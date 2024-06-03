package com.colabear754.spring_rest_docs_demo.services

import com.colabear754.spring_rest_docs_demo.dto.MemberDeleteResponse
import com.colabear754.spring_rest_docs_demo.dto.MemberInfoResponse
import com.colabear754.spring_rest_docs_demo.dto.MemberUpdateRequest
import com.colabear754.spring_rest_docs_demo.dto.MemberUpdateResponse
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import com.colabear754.spring_rest_docs_demo.util.findByIdOrThrow
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val encoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun getMemberInfo(id: UUID) = MemberInfoResponse.from(memberRepository.findByIdOrThrow(id, "존재하지 않는 회원입니다."))

    @Transactional
    fun deleteMember(id: UUID): MemberDeleteResponse {
        if (!memberRepository.existsById(id)) return MemberDeleteResponse(false)
        memberRepository.deleteById(id)
        return MemberDeleteResponse(true)
    }

    @Transactional
    fun updateMember(id: UUID, request: MemberUpdateRequest): MemberUpdateResponse {
        val member = memberRepository.findByIdOrNull(id)?.takeIf { encoder.matches(request.password, it.password) }
            ?: throw IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.")
        member.update(request, encoder)
        return MemberUpdateResponse.of(true, member)
    }
}