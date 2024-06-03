package com.colabear754.spring_rest_docs_demo.services

import com.colabear754.spring_rest_docs_demo.common.MemberType
import com.colabear754.spring_rest_docs_demo.dto.MemberInfoResponse
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(private val memberRepository: MemberRepository) {
    @Transactional(readOnly = true)
    fun getMembers(): List<MemberInfoResponse> = memberRepository.findAllByType(MemberType.USER).map(MemberInfoResponse::from)

    @Transactional(readOnly = true)
    fun getAdmins(): List<MemberInfoResponse> = memberRepository.findAllByType(MemberType.ADMIN).map(MemberInfoResponse::from)
}