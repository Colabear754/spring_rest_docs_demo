package com.colabear754.spring_rest_docs_demo.controllers

import com.colabear754.spring_rest_docs_demo.common.ApiStatus
import com.colabear754.spring_rest_docs_demo.common.MemberType
import com.colabear754.spring_rest_docs_demo.entity.Member
import com.colabear754.spring_rest_docs_demo.extension.requestPreprocessor
import com.colabear754.spring_rest_docs_demo.extension.responsePreprocessor
import com.colabear754.spring_rest_docs_demo.repositories.MemberRepository
import com.colabear754.spring_rest_docs_demo.security.TokenProvider
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.demo.com", uriPort = 0)
class AdminControllerTest @Autowired constructor(
    private val tokenProvider: TokenProvider,
    private val memberRepository: MemberRepository,
    private val mockMvc: MockMvc
) {
    @BeforeEach
    fun clear() {
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모든 사용자 찾기`() {
        val token = "Bearer ${tokenProvider.createAccessToken("admin:ADMIN")}"
        // given
        val uuids = memberRepository.saveAll(
            listOf(
                Member("colabear754", "1234", "콜라곰", type = MemberType.USER),
                Member("ciderbear754", "1234", "사이다곰", type = MemberType.USER),
                Member("fantabear754", "1234", "환타곰", type = MemberType.USER)
            )
        ).map { it.id.toString() }
        // when
        val actionsDsl = mockMvc.get("/admin/members") { header(HttpHeaders.AUTHORIZATION, token) }
        // then
        actionsDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data", Matchers.hasSize<Any>(3))
            jsonPath("$.data[*].id", Matchers.containsInAnyOrder(uuids[0], uuids[1], uuids[2]))
            jsonPath("$.data[*].name", Matchers.containsInAnyOrder("콜라곰", "사이다곰", "환타곰"))
            jsonPath("$.data[*].type", Matchers.containsInAnyOrder("USER", "USER", "USER"))
        }

        // Spring REST Docs
        actionsDsl.andDo { handle(
            document(
                "admin/members",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")),
                responsePreprocessor(),
                memberInfoResponseSnippet
            )
        ) }
    }

    @Test
    fun `모든 관리자 찾기`() {
        // given
        memberRepository.save(Member("admin", "admin", "관리자", type = MemberType.ADMIN))
        val token = "Bearer ${tokenProvider.createAccessToken("admin:ADMIN")}"
        val uuid = memberRepository.findByAccount("admin")?.id?.toString()
        // when
        val actionsDsl = mockMvc.get("/admin/admins") { header(HttpHeaders.AUTHORIZATION, token) }
        // then
        actionsDsl.andExpect {
            status { isOk() }
            jsonPath("$.status", Matchers.`is`(ApiStatus.SUCCESS.name))
            jsonPath("$.message", Matchers.nullValue())
            jsonPath("$.data", Matchers.hasSize<Any>(1))
            jsonPath("$.data[*].id", Matchers.containsInAnyOrder(uuid))
            jsonPath("$.data[*].name", Matchers.containsInAnyOrder("관리자"))
            jsonPath("$.data[*].type", Matchers.containsInAnyOrder("ADMIN"))
        }

        // Spring REST Docs
        actionsDsl.andDo { handle(
            document(
                "admin/admins",
                requestPreprocessor(modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")),
                responsePreprocessor(),
                memberInfoResponseSnippet
            )
        ) }
    }

    private val memberInfoResponseSnippet: ResponseFieldsSnippet? = responseFields(
        fieldWithPath("status").description("API 상태"),
        fieldWithPath("message").description("API 메시지").type(JsonFieldType.STRING).optional(),
        fieldWithPath("data").description("응답 데이터"),
        fieldWithPath("data[].id").description("사용자 ID"),
        fieldWithPath("data[].account").description("계정"),
        fieldWithPath("data[].name").description("이름"),
        fieldWithPath("data[].age").description("나이").type(JsonFieldType.NUMBER).optional(),
        fieldWithPath("data[].type").description("사용자 유형"),
        fieldWithPath("data[].createdAt").description("가입일")
    )
}