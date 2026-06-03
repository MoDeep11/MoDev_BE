package modeep.modev.domain.project.controller

import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.response.GetProjectsResponse
import modeep.modev.domain.project.controller.dto.response.PaginationResponse
import modeep.modev.domain.project.controller.dto.response.ProjectSummaryResponse
import modeep.modev.domain.project.controller.dto.response.SaveProjectResponse
import modeep.modev.domain.project.service.ProjectService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(ProjectController::class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var projectService: ProjectService

    @Test
    fun `gets project list and returns api response`() {
        Mockito
            .`when`(projectService.getProjects(1, 20, "spring"))
            .thenReturn(
                GetProjectsResponse(
                    projects =
                        listOf(
                            ProjectSummaryResponse(
                                projectId = "proj_xyz",
                                projectName = "my-project",
                                description = "project description",
                                stacks = listOf("Spring Boot", "React", "MySQL"),
                                createdAt = Instant.parse("2025-05-26T10:00:00Z"),
                                updatedAt = Instant.parse("2025-05-26T10:00:00Z"),
                                status = "ACTIVE",
                            ),
                        ),
                    pagination =
                        PaginationResponse(
                            currentPage = 1,
                            totalPages = 3,
                            totalCount = 42,
                            size = 20,
                        ),
                ),
            )

        mockMvc
            .get("/projects") {
                param("page", "1")
                param("size", "20")
                param("keyword", "spring")
            }.andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.projects[0].projectId") { value("proj_xyz") }
                jsonPath("$.data.projects[0].projectName") { value("my-project") }
                jsonPath("$.data.projects[0].description") { value("project description") }
                jsonPath("$.data.projects[0].stacks[0]") { value("Spring Boot") }
                jsonPath("$.data.projects[0].createdAt") { value("2025-05-26T10:00:00Z") }
                jsonPath("$.data.projects[0].updatedAt") { value("2025-05-26T10:00:00Z") }
                jsonPath("$.data.projects[0].status") { value("ACTIVE") }
                jsonPath("$.data.pagination.currentPage") { value(1) }
                jsonPath("$.data.pagination.totalPages") { value(3) }
                jsonPath("$.data.pagination.totalCount") { value(42) }
                jsonPath("$.data.pagination.size") { value(20) }
                jsonPath("$.error") { doesNotExist() }
            }
    }

    @Test
    fun `saves project and returns api response`() {
        Mockito
            .`when`(
                projectService.saveProject(
                    SaveProjectRequest(
                        generateId = "gen_abc123",
                        projectName = "my-project",
                        description = "프로젝트 설명",
                    ),
                ),
            ).thenReturn(
                SaveProjectResponse(
                    projectId = "proj_xyz",
                    projectName = "my-project",
                    createdAt = Instant.parse("2025-05-26T10:00:00Z"),
                ),
            )

        mockMvc
            .post("/projects") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "generateId": "gen_abc123",
                      "projectName": "my-project",
                      "description": "프로젝트 설명"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isCreated() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.projectId") { value("proj_xyz") }
                jsonPath("$.data.projectName") { value("my-project") }
                jsonPath("$.data.createdAt") { value("2025-05-26T10:00:00Z") }
                jsonPath("$.error") { doesNotExist() }
            }
    }
}
