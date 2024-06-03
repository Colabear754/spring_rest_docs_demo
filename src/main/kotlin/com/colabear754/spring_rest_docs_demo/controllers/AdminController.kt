package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.dto.ApiResponse
import com.colabear754.spring_rest_docs_demo.security.AdminAuthorize
import com.colabear754.spring_rest_docs_demo.services.AdminService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@AdminAuthorize
@RestController
@RequestMapping("/admin")
class AdminController(private val adminService: AdminService) {
    @GetMapping("/members")
    fun getAllMembers() = ApiResponse.success(adminService.getMembers())

    @GetMapping("/admins")
    fun getAllAdmins() = ApiResponse.success(adminService.getAdmins())
}