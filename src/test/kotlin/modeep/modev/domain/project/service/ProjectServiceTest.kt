package modeep.modev.domain.project.service

import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectServiceTest {
    private val projectRepository = Mockito.mock(ProjectRepository::class.java)
    private val projectService = ProjectService(projectRepository)

    @Test
    fun `gets projects with keyword and returns paged project summaries`() {
        val createdAt = Instant.parse("2025-05-26T10:00:00Z")
        val project =
            Project(
                projectId = "proj_xyz",
                generateId = "gen_abc123",
                projectName = "my-project",
                description = "project description",
                createdAt = createdAt,
            )
        val expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        Mockito
            .`when`(
                projectRepository.findByProjectNameContainingIgnoreCase(
                    "spring",
                    expectedPageable,
                ),
            ).thenReturn(PageImpl(listOf(project), PageRequest.of(0, 20), 42))

        val response = projectService.getProjects(page = 1, size = 20, keyword = " spring ")

        assertEquals(1, response.projects.size)
        assertEquals("proj_xyz", response.projects[0].projectId)
        assertEquals("my-project", response.projects[0].projectName)
        assertEquals("project description", response.projects[0].description)
        assertEquals(emptyList(), response.projects[0].stacks)
        assertEquals(createdAt, response.projects[0].createdAt)
        assertEquals(createdAt, response.projects[0].updatedAt)
        assertEquals("ACTIVE", response.projects[0].status)
        assertEquals(1, response.pagination.currentPage)
        assertEquals(3, response.pagination.totalPages)
        assertEquals(42, response.pagination.totalCount)
        assertEquals(20, response.pagination.size)

        Mockito.verify(projectRepository).findByProjectNameContainingIgnoreCase("spring", expectedPageable)
    }

    @Test
    fun `gets projects without keyword using default find all`() {
        val expectedPageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"))

        Mockito
            .`when`(projectRepository.findAll(expectedPageable))
            .thenReturn(PageImpl(emptyList(), PageRequest.of(1, 10), 0))

        val response = projectService.getProjects(page = 2, size = 10, keyword = null)

        assertEquals(emptyList(), response.projects)
        assertEquals(2, response.pagination.currentPage)
        assertEquals(0, response.pagination.totalPages)
        assertEquals(0, response.pagination.totalCount)
        assertEquals(10, response.pagination.size)
        Mockito.verify(projectRepository).findAll(expectedPageable)
    }

    @Test
    fun `saves project and returns saved project data`() {
        Mockito
            .`when`(projectRepository.save(Mockito.any(Project::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Project }

        val response =
            projectService.saveProject(
                SaveProjectRequest(
                    generateId = "gen_abc123",
                    projectName = "my-project",
                    description = "프로젝트 설명",
                ),
            )

        assertTrue(response.projectId.startsWith("proj_"))
        assertEquals("my-project", response.projectName)
        assertNotNull(response.createdAt)

        val savedProject =
            Mockito.mockingDetails(projectRepository).invocations
                .first { it.method.name == "save" }
                .arguments[0] as Project
        assertEquals(response.projectId, savedProject.projectId)
        assertEquals("gen_abc123", savedProject.generateId)
        assertEquals("my-project", savedProject.projectName)
        assertEquals("프로젝트 설명", savedProject.description)
        assertEquals(response.createdAt, savedProject.createdAt)
    }
}
