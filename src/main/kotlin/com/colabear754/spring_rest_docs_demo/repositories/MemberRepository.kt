package com.colabear754.spring_rest_docs_demo.repositories

import com.colabear754.spring_rest_docs_demo.common.MemberType
import com.colabear754.spring_rest_docs_demo.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, UUID> {
    fun findByAccount(account: String): Member?
    fun findAllByType(type: MemberType): List<Member>
}