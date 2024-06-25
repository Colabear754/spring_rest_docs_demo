package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.common.ApiStatus
import com.colabear754.spring_rest_docs_demo.common.MemberType
import com.colabear754.spring_rest_docs_demo.dto.SignInRequest
import com.colabear754.spring_rest_docs_demo.dto.SignUpRequest
import com.colabear754.spring_rest_docs_demo.entity.Member
import com.colabear754.spring_rest_docs_demo.extension.requestPreprocessor
import com.colabear754.spring_rest_docs_demo.extension.responsePreprocessor
import com.colabear754.spring_rest_docs_demo.repositories.MemberRefreshTokenRepository
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.demo.com", uriPort = 0)
class SignControllerTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
    private val mockMvc: MockMvc,
    private val encoder: PasswordEncoder,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {
    @BeforeEach
    fun clear() {
        memberRefreshTokenRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun 회원가입() {
        // given
         val request = SignUpRequest("colabear754", "1234", "콜라곰", 99)
        // when
        val actionsDsl = mockMvc.post("/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        // then
        actionsDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data.id", Matchers.notNullValue())
            jsonPath("$.data.account", Matchers.`is`("colabear754"))
            jsonPath("$.data.name", Matchers.`is`("콜라곰"))
            jsonPath("$.data.age", Matchers.`is`(99))
        }

        // Spring REST Docs
        actionsDsl.andDo { handle(
            document(
                "sign-up",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)),
                responsePreprocessor(),
                requestFields(
                    fieldWithPath("account").description("사용자 계정"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("사용자 이름").optional(),
                    fieldWithPath("age").description("사용자 나이").optional()
                ),
                responseFields(
                    fieldWithPath("status").description("응답 상태"),
                    fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data.id").description("사용자 ID"),
                    fieldWithPath("data.account").description("사용자 계정"),
                    fieldWithPath("data.name").description("사용자 이름").optional(),
                    fieldWithPath("data.age").description("사용자 나이").optional()
                )
            )
        ) }
    }

    @Test
    fun 로그인() {
        // given
        memberRepository.save(Member("colabear754", encoder.encode("1234"), "콜라곰"))
        val request = SignInRequest("colabear754", "1234")
        // when
        val actionsDsl = mockMvc.post("/sign-in") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        // then
        actionsDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data.name", Matchers.`is`("콜라곰"))
            jsonPath("$.data.type", Matchers.`is`(MemberType.USER.name))
            jsonPath("$.data.accessToken", Matchers.notNullValue())
            jsonPath("$.data.refreshToken", Matchers.notNullValue())
        }

        // Spring REST Docs
        actionsDsl.andDo { handle(
            document(
                "sign-in",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)),
                responsePreprocessor(),
                requestFields(
                    fieldWithPath("account").description("사용자 계정"),
                    fieldWithPath("password").description("비밀번호")
                ),
                responseFields(
                    fieldWithPath("status").description("응답 상태"),
                    fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data.name").description("사용자 이름"),
                    fieldWithPath("data.type").description("사용자 타입"),
                    fieldWithPath("data.accessToken").description("액세스 토큰"),
                    fieldWithPath("data.refreshToken").description("리프레시 토큰")
                )
            )
        ) }
    }
}
