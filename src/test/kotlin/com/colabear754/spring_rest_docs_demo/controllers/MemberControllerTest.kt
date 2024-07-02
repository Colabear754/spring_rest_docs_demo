package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.common.ApiStatus
import com.colabear754.spring_rest_docs_demo.dto.MemberUpdateRequest
import com.colabear754.spring_rest_docs_demo.entity.Member
import com.colabear754.spring_rest_docs_demo.extension.requestPreprocessor
import com.colabear754.spring_rest_docs_demo.extension.responsePreprocessor
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import com.colabear754.spring_rest_docs_demo.security.TokenProvider
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.demo.com", uriPort = 0)
class MemberControllerTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val tokenProvider: TokenProvider,
    private val mockMvc: MockMvc,
    private val encoder: PasswordEncoder,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {
    @BeforeEach
    fun clear() {
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `로그인한 사용자 정보 조회`() {
        // given
        val member = memberRepository.save(Member("colabear754", "1234", "콜라곰"))
        val token = tokenProvider.createAccessToken("${member.id}:USER")
        // when
        val actionDsl = mockMvc.get("/member") { header(HttpHeaders.AUTHORIZATION, "Bearer $token") }
        // then
        actionDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data.id", Matchers.`is`(member.id.toString()))
            jsonPath("$.data.account", Matchers.`is`("colabear754"))
            jsonPath("$.data.name", Matchers.`is`("콜라곰"))
            jsonPath("$.data.age", Matchers.nullValue())
            jsonPath("$.data.type", Matchers.`is`("USER"))
            jsonPath("$.data.createdAt", Matchers.notNullValue())
        }

        // Spring REST Docs
        actionDsl.andDo { handle(
            document(
                "member/info",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")),
                responsePreprocessor(),
                responseFields(
                    fieldWithPath("status").description("응답 상태"),
                    fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data").description("응답 데이터"),
                    fieldWithPath("data.id").description("사용자 ID"),
                    fieldWithPath("data.account").description("계정"),
                    fieldWithPath("data.name").description("이름").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data.age").description("나이").type(JsonFieldType.NUMBER).optional(),
                    fieldWithPath("data.type").description("사용자 타입"),
                    fieldWithPath("data.createdAt").description("가입 일시")
                )
            )
        ) }
    }

    @Test
    fun `로그인한 사용자 탈퇴`() {
        // given
        val token = memberRepository.save(Member("colabear754", "1234", "콜라곰"))
            .run { tokenProvider.createAccessToken("$id:USER") }
        // when
        val actionDsl = mockMvc.delete("/member") { header(HttpHeaders.AUTHORIZATION, "Bearer $token") }
        // then
        actionDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data.result", Matchers.`is`(true))
        }

        // Spring REST Docs
        actionDsl.andDo { handle(
            document(
                "member/delete",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")),
                responsePreprocessor(),
                responseFields(
                    fieldWithPath("status").description("응답 상태"),
                    fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data").description("응답 데이터"),
                    fieldWithPath("data.result").description("탈퇴 결과")
                )
            )
        ) }
    }

    @Test
    fun `로그인한 사용자 정보 수정`() {
        // given
        val member = memberRepository.save(Member("colabear754", encoder.encode("1234"), "콜라곰"))
        val token = tokenProvider.createAccessToken("${member.id}:USER")
        val request = MemberUpdateRequest("1234", name = "콜라곰곰", age = 100)
        // when
        val actionDsl = mockMvc.put("/member") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        // then
        actionDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data.result", Matchers.`is`(true))
            jsonPath("$.data.name", Matchers.`is`("콜라곰곰"))
            jsonPath("$.data.age", Matchers.`is`(100))
        }

        // Spring REST Docs
        actionDsl.andDo { handle(
            document(
                "member/update",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")),
                responsePreprocessor(),
                requestFields(
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("newPassword").description("새로운 비밀번호").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("name").description("이름").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("age").description("나이").type(JsonFieldType.NUMBER).optional()
                ),
                responseFields(
                    fieldWithPath("status").description("응답 상태"),
                    fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data").description("응답 데이터"),
                    fieldWithPath("data.result").description("수정 결과"),
                    fieldWithPath("data.name").description("이름").type(JsonFieldType.STRING).optional(),
                    fieldWithPath("data.age").description("나이").type(JsonFieldType.NUMBER).optional()
                )
            )
        ) }
    }
}