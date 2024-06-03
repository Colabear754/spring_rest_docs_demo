package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.dto.ApiResponse
import com.colabear754.spring_rest_docs_demo.dto.MemberUpdateRequest
import com.colabear754.spring_rest_docs_demo.security.UserAuthorize
import com.colabear754.spring_rest_docs_demo.services.MemberService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*
import java.util.*

@UserAuthorize
@RestController
@RequestMapping("/member")
class MemberController(private val memberService: MemberService) {
    @GetMapping
    fun getMemberInfo(@AuthenticationPrincipal user: User) =
        ApiResponse.success(memberService.getMemberInfo(UUID.fromString(user.username)))

    @DeleteMapping
    fun deleteMember(@AuthenticationPrincipal user: User) =
        ApiResponse.success(memberService.deleteMember(UUID.fromString(user.username)))

    @PutMapping
    fun updateMember(@AuthenticationPrincipal user: User, @RequestBody request: MemberUpdateRequest) =
        ApiResponse.success(memberService.updateMember(UUID.fromString(user.username), request))
}