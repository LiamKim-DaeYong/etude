package com.etude.board.web

import com.etude.IntegrationTest
import com.etude.TestCompanies
import com.etude.TestCompany
import com.etude.assertProblem
import com.etude.inTransaction
import com.etude.update
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.util.UUID
import javax.sql.DataSource

@IntegrationTest
class PostAdminControllerTest(
    @Autowired private val mvc: MockMvcTester,
    @Autowired private val companies: TestCompanies,
    @Autowired private val dataSource: DataSource,
) {

    private val json = JsonMapper.builder().build()

    @Test
    fun `writes, reads, edits and deletes a post`() {
        val company = companies.create()
        val session = companies.ownerSession(company)
        val boardId = defaultBoardId(session)

        val written = write(session, boardId, "Shift change", "Night shift starts at 22:00.")

        assertThat(written).hasStatus(HttpStatus.CREATED)
        val postId = body(written)["id"].asString()
        assertThat(written.mvcResult.response.getHeader("Location")).isEqualTo("/admin/v1/boards/$boardId/posts/$postId")
        assertThat(written).bodyJson().isLenientlyEqualTo(
            """{"boardId":"$boardId","title":"Shift change","body":"Night shift starts at 22:00.",
               "authorId":"${company.ownerId}","authorUsername":"owner","editedAt":null,"editable":true}""",
        )
        assertThat(body(written)["postedAt"].asString()).endsWith("Z")

        assertThat(get(session, boardId, postId)).hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"id":"$postId","title":"Shift change","editable":true}""")
        assertThat(listPosts(session, boardId)).hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"items":[{"id":"$postId","authorUsername":"owner"}],"page":0,"size":20,"totalElements":1,"totalPages":1}""")
        assertThat(recentPosts(session)).hasStatusOk().bodyJson().isLenientlyEqualTo("""[{"id":"$postId","boardId":"$boardId"}]""")

        val edited = edit(session, boardId, postId, "Shift change (updated)", "Night shift starts at 23:00.")
        assertThat(edited).hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"id":"$postId","title":"Shift change (updated)","body":"Night shift starts at 23:00."}""")
        assertThat(body(edited)["editedAt"].asString()).endsWith("Z")

        assertThat(delete(session, boardId, postId)).hasStatus(HttpStatus.NO_CONTENT)

        assertProblem(get(session, boardId, postId), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertThat(listPosts(session, boardId)).bodyJson().isLenientlyEqualTo("""{"items":[],"totalElements":0}""")
        assertThat(recentPosts(session)).bodyJson().isEqualTo("[]")
        assertProblem(edit(session, boardId, postId, "Again", "Again"), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(delete(session, boardId, postId), HttpStatus.NOT_FOUND, "NOT_FOUND")
    }

    @Test
    fun `staff of the same company read and write posts but change only their own`() {
        val company = companies.create()
        val owner = companies.ownerSession(company)
        val staff = companies.staffSession(company, "kim")
        val boardId = defaultBoardId(owner)
        val ownerPost = postId(write(owner, boardId, "Owner notice", "Read me."))

        assertThat(get(staff, boardId, ownerPost)).hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"authorUsername":"owner","editable":false}""")
        val staffWritten = write(staff, boardId, "Staff note", "Machine 3 needs oil.")
        assertThat(staffWritten).hasStatus(HttpStatus.CREATED).bodyJson().isLenientlyEqualTo("""{"authorUsername":"kim","editable":true}""")
        val staffPost = postId(staffWritten)

        assertProblem(edit(staff, boardId, ownerPost, "Changed", "Changed"), HttpStatus.FORBIDDEN, "NOT_POST_AUTHOR")
        assertProblem(delete(staff, boardId, ownerPost), HttpStatus.FORBIDDEN, "NOT_POST_AUTHOR")
        // 대표도 첫 버전에서는 다른 사람의 글을 바꾸지 못한다.
        assertProblem(edit(owner, boardId, staffPost, "Changed", "Changed"), HttpStatus.FORBIDDEN, "NOT_POST_AUTHOR")
        assertProblem(delete(owner, boardId, staffPost), HttpStatus.FORBIDDEN, "NOT_POST_AUTHOR")

        assertThat(get(owner, boardId, ownerPost)).bodyJson().isLenientlyEqualTo("""{"title":"Owner notice","editedAt":null}""")
        assertThat(get(owner, boardId, staffPost)).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"title":"Staff note"}""")
        assertThat(listPosts(staff, boardId)).bodyJson().isLenientlyEqualTo(
            """{"items":[{"id":"$staffPost"},{"id":"$ownerPost"}],"totalElements":2}""",
        )
    }

    @Test
    fun `another company can neither see nor change the posts`() {
        val acme = companies.create("Acme Film")
        val acmeSession = companies.ownerSession(acme)
        val acmeBoard = defaultBoardId(acmeSession)
        val acmePost = postId(write(acmeSession, acmeBoard, "Acme only", "Internal."))

        val other = companies.create("Other Film")
        val otherSession = companies.staffSession(other)
        val otherBoard = defaultBoardId(otherSession)

        assertThat(otherBoard).isNotEqualTo(acmeBoard)
        assertThat(boards(otherSession)).bodyJson().isLenientlyEqualTo("""[{"id":"$otherBoard"}]""")
        assertProblem(listPosts(otherSession, acmeBoard), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(get(otherSession, acmeBoard, acmePost), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(get(otherSession, otherBoard, acmePost), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(write(otherSession, acmeBoard, "Intruder", "Hello"), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(edit(otherSession, acmeBoard, acmePost, "Changed", "Changed"), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(delete(otherSession, acmeBoard, acmePost), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertThat(recentPosts(otherSession)).bodyJson().isEqualTo("[]")

        assertThat(get(acmeSession, acmeBoard, acmePost)).hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"title":"Acme only","body":"Internal.","editedAt":null}""")
        assertThat(listPosts(acmeSession, acmeBoard)).bodyJson().isLenientlyEqualTo("""{"totalElements":1}""")
    }

    @Test
    fun `finds a post only under its own board`() {
        val company = companies.create()
        val session = companies.ownerSession(company)
        val defaultBoard = defaultBoardId(session)
        val otherBoard = addBoard(company, "Notices")
        val postId = postId(write(session, defaultBoard, "Default board post", "Hello"))

        assertThat(boards(session)).bodyJson().isEqualTo(
            """[{"id":"$defaultBoard","name":null,"default":true},{"id":"$otherBoard","name":"Notices","default":false}]""",
        )
        assertProblem(get(session, otherBoard, postId), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(edit(session, otherBoard, postId, "Moved", "Moved"), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertProblem(delete(session, otherBoard, postId), HttpStatus.NOT_FOUND, "NOT_FOUND")
        assertThat(listPosts(session, otherBoard)).bodyJson().isLenientlyEqualTo("""{"items":[],"totalElements":0}""")
        assertThat(get(session, defaultBoard, postId)).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"title":"Default board post"}""")
    }

    @Test
    fun `requires sign-in and a CSRF token`() {
        val company = companies.create()
        val session = companies.ownerSession(company)
        val boardId = defaultBoardId(session)
        val postId = postId(write(session, boardId, "Title", "Body"))

        assertProblem(mvc.get().uri("/admin/v1/boards").exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
        assertProblem(mvc.get().uri("/admin/v1/posts").exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
        assertProblem(mvc.get().uri(POST, boardId, postId).exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")

        assertProblem(
            mvc.post().uri(POSTS, boardId).session(session).contentType(MediaType.APPLICATION_JSON).content(content("Title", "Body")).exchange(),
            HttpStatus.FORBIDDEN,
            "CSRF_TOKEN_INVALID",
        )
        assertProblem(
            mvc.put().uri(POST, boardId, postId).session(session).contentType(MediaType.APPLICATION_JSON).content(content("Changed", "Changed")).exchange(),
            HttpStatus.FORBIDDEN,
            "CSRF_TOKEN_INVALID",
        )
        assertProblem(mvc.delete().uri(POST, boardId, postId).session(session).exchange(), HttpStatus.FORBIDDEN, "CSRF_TOKEN_INVALID")

        assertThat(get(session, boardId, postId)).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"title":"Title"}""")
    }

    @Test
    fun `validates the title, body and paging`() {
        val company = companies.create()
        val session = companies.ownerSession(company)
        val boardId = defaultBoardId(session)

        assertProblem(write(session, boardId, " ", "Body"), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(write(session, boardId, "x".repeat(201), "Body"), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(write(session, boardId, "Title", ""), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(write(session, boardId, "Title", "x".repeat(20_001)), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertThat(write(session, boardId, "x".repeat(200), "x".repeat(20_000))).hasStatus(HttpStatus.CREATED)

        assertProblem(mvc.get().uri("$POSTS?size=0", boardId).session(session).exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(mvc.get().uri("$POSTS?size=101", boardId).session(session).exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(mvc.get().uri("$POSTS?page=-1", boardId).session(session).exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(mvc.get().uri("/admin/v1/posts?size=21").session(session).exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        assertProblem(mvc.get().uri(POSTS, "not-a-uuid").session(session).exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
    }

    @Test
    fun `lists posts newest first, a page at a time`() {
        val company = companies.create()
        val session = companies.ownerSession(company)
        val boardId = defaultBoardId(session)
        val first = postId(write(session, boardId, "First", "1"))
        val second = postId(write(session, boardId, "Second", "2"))
        val third = postId(write(session, boardId, "Third", "3"))

        assertThat(mvc.get().uri("$POSTS?page=0&size=2", boardId).session(session).exchange()).hasStatusOk().bodyJson().isLenientlyEqualTo(
            """{"items":[{"id":"$third"},{"id":"$second"}],"page":0,"size":2,"totalElements":3,"totalPages":2}""",
        )
        assertThat(mvc.get().uri("$POSTS?page=1&size=2", boardId).session(session).exchange()).hasStatusOk().bodyJson().isLenientlyEqualTo(
            """{"items":[{"id":"$first"}],"page":1,"size":2,"totalElements":3,"totalPages":2}""",
        )
        assertThat(recentIds(session, size = 2)).containsExactly(third, second)
    }

    private fun defaultBoardId(session: MockHttpSession): String {
        // 기본 게시판은 회사 등록 이벤트를 받아 비동기로 준비된다.
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            assertThat(body(boards(session)).size()).isEqualTo(1)
        }
        return body(boards(session))[0]["id"].asString()
    }

    /** 같은 회사에 두 번째 게시판을 만든다. 게시판을 만드는 API가 없어 애플리케이션 역할로 직접 넣는다(RLS 통과). */
    private fun addBoard(company: TestCompany, name: String): String {
        val id = UUID.randomUUID()
        dataSource.inTransaction(company.tenantId) {
            it.update(
                "insert into board (id, tenant_id, name, is_default, created_at, updated_at) values (?, ?, ?, false, now(), now())",
                id,
                company.tenantId,
                name,
            )
        }
        return id.toString()
    }

    private fun boards(session: MockHttpSession) = mvc.get().uri("/admin/v1/boards").session(session).exchange()

    private fun listPosts(session: MockHttpSession, boardId: String) = mvc.get().uri(POSTS, boardId).session(session).exchange()

    private fun recentPosts(session: MockHttpSession) = mvc.get().uri("/admin/v1/posts").session(session).exchange()

    private fun recentIds(session: MockHttpSession, size: Int): List<String> {
        val items = body(mvc.get().uri("/admin/v1/posts?size=$size").session(session).exchange())
        return (0 until items.size()).map { items[it]["id"].asString() }
    }

    private fun get(session: MockHttpSession, boardId: String, postId: String) = mvc.get().uri(POST, boardId, postId).session(session).exchange()

    private fun write(session: MockHttpSession, boardId: String, title: String, body: String) = mvc.post().uri(POSTS, boardId).session(session).with(csrf())
        .contentType(MediaType.APPLICATION_JSON).content(content(title, body)).exchange()

    private fun edit(session: MockHttpSession, boardId: String, postId: String, title: String, body: String) = mvc.put().uri(POST, boardId, postId).session(session).with(csrf())
        .contentType(MediaType.APPLICATION_JSON).content(content(title, body)).exchange()

    private fun delete(session: MockHttpSession, boardId: String, postId: String) = mvc.delete().uri(POST, boardId, postId).session(session).with(csrf()).exchange()

    private fun content(title: String, body: String) = json.writeValueAsString(mapOf("title" to title, "body" to body))

    private fun postId(result: MvcTestResult): String {
        assertThat(result).hasStatus(HttpStatus.CREATED)
        return body(result)["id"].asString()
    }

    private fun body(result: MvcTestResult): JsonNode = json.readTree(result.mvcResult.response.contentAsString)

    private companion object {
        const val POSTS = "/admin/v1/boards/{boardId}/posts"
        const val POST = "/admin/v1/boards/{boardId}/posts/{postId}"
    }
}
