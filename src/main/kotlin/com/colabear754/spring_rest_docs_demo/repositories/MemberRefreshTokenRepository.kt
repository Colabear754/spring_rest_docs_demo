package com.colabear754.spring_rest_docs_demo.repositories

import com.colabear754.spring_rest_docs_demo.entity.MemberRefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRefreshTokenRepository : JpaRepository<MemberRefreshToken, UUID> {
    fun findByMemberIdAndReissueCountLessThan(id: UUID, count: Long): MemberRefreshToken?
}