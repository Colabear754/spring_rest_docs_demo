package com.colabear754.spring_rest_docs_demo.dto

data class SignUpRequest(
    val account: String,
    var password: String,
    val name: String? = null,
    val age: Int? = null
)

data class SignInRequest(
    val account: String,
    val password: String
)

data class MemberUpdateRequest(
    var password: String,
    var newPassword: String? = null,
    val name: String? = null,
    val age: Int? = null
)