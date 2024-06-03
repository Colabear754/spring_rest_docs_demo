package com.colabear754.spring_rest_docs_demo.dto

import com.colabear754.spring_rest_docs_demo.common.MemberType
import com.colabear754.spring_rest_docs_demo.entity.Member
import java.time.LocalDateTime
import java.util.*

data class SignUpResponse(
    val id: UUID,
    val account: String,
    val name: String?,
    val age: Int?
) {
    companion object {
        fun from(member: Member) = SignUpResponse(
            id = member.id!!,
            account = member.account,
            name = member.name,
            age = member.age
        )
    }
}

data class SignInResponse(
    val name: String?,
    val type: MemberType,
    val accessToken: String,
    val refreshToken: String
)

data class MemberUpdateResponse(
    val result: Boolean,
    val name: String?,
    val age: Int?
) {
    companion object {
        fun of(result: Boolean, member: Member) = MemberUpdateResponse(
            result = result,
            name = member.name,
            age = member.age
        )
    }
}

data class MemberInfoResponse(
    val id: UUID,
    val account: String,
    val name: String?,
    val age: Int?,
    val type: MemberType,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(member: Member) = MemberInfoResponse(
            id = member.id!!,
            account = member.account,
            name = member.name,
            age = member.age,
            type = member.type,
            createdAt = member.createdAt
        )
    }
}

data class MemberDeleteResponse(
    val result: Boolean
)