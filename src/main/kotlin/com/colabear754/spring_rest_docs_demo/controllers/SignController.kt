package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.dto.ApiResponse
import com.colabear754.spring_rest_docs_demo.dto.SignInRequest
import com.colabear754.spring_rest_docs_demo.dto.SignUpRequest
import com.colabear754.spring_rest_docs_demo.services.SignService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class SignController(private val signService: SignService) {
    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpRequest) = ApiResponse.success(signService.registMember(request))

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: SignInRequest) = ApiResponse.success(signService.signIn(request))
}