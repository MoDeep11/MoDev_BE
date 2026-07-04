package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.ProjectStackSummary
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.response.GetStructureStatusResponse
import modeep.modev.domain.structure.controller.dto.response.StructureResultResponse
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var fieldRepository: FieldRepository
    private lateinit var dependencyRepository: DependencyRepository
    private lateinit var techStackRepository: TechStackRepository
    private lateinit var getStructureStatusService: GetStructureStatusService
    private lateinit var service: GetProjectService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        fieldRepository = mock(FieldRepository::class.java)
        dependencyRepository = mock(DependencyRepository::class.java)
        techStackRepository = mock(TechStackRepository::class.java)
        getStructureStatusService = mock(GetStructureStatusService::class.java)
        service =
            GetProjectService(
                projectRepository = projectRepository,
                fieldRepository = fieldRepository,
                dependencyRepository = dependencyRepository,
                techStackRepository = techStackRepository,
                getStructureStatusService = getStructureStatusService,
            )
    }

    @Test
    fun `returns project detail with catalogs and file tree`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "modev", description = "description")
        val backend = Field(id = 1L, publicId = "backend", name = "Backend")
        val spring =
            TechStack(
                id = 1L,
                publicId = "spring",
                name = "Spring Boot",
                version = "3.5.0",
                category = Category.FRAMEWORK,
                field = backend,
            )
        val security =
            Dependency(
                id = 1L,
                publicId = "spring-security",
                techStack = spring,
                name = "Spring Security",
                version = "6.5.0",
            )
        val fileTree = GetStructureStatusResponse(projectId, "COMPLETED", StructureResultResponse(emptyList()))

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(project)
        `when`(fieldRepository.findByProjectId(projectId)).thenReturn(listOf(backend))
        `when`(techStackRepository.findByProjectId(projectId)).thenReturn(listOf(spring))
        `when`(dependencyRepository.findByProjectId(projectId)).thenReturn(listOf(security))
        `when`(getStructureStatusService.execute(projectId)).thenReturn(fileTree)

        val response = service.getProjectDetail(projectId)

        assertEquals(projectId, response.projectId)
        assertEquals("modev", response.projectName)
        assertEquals(listOf("Backend"), response.fields)
        assertEquals("spring", response.stacks.single().stackId)
        assertEquals("Spring Boot", response.stacks.single().name)
        assertEquals("FRAMEWORK", response.stacks.single().category)
        assertEquals("spring-security", response.dependencies.single().dependencyId)
        assertEquals("spring", response.dependencies.single().stackId)
        assertEquals(fileTree, response.fileTree)
    }

    @Test
    fun `throws project not found when project detail does not exist`() {
        val projectId = UUID.randomUUID()

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.getProjectDetail(projectId)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
        verifyNoInteractions(fieldRepository, techStackRepository, dependencyRepository, getStructureStatusService)
    }

    @Test
    fun `returns paged projects using normalized keyword and page bounds`() {
        val firstProjectId = UUID.randomUUID()
        val secondProjectId = UUID.randomUUID()
        val firstProject = Project(id = firstProjectId, projectName = "modev api", description = "first")
        val secondProject = Project(id = secondProjectId, projectName = "modev web", description = null)
        val pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
        val projects = PageImpl(listOf(firstProject, secondProject), pageable, 2)

        `when`(
            projectRepository.findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull(
                "modev",
                pageable,
            ),
        ).thenReturn(projects)
        `when`(techStackRepository.findByProjectIdIn(listOf(firstProjectId, secondProjectId)))
            .thenReturn(
                listOf(
                    ProjectStackSummary(firstProjectId, "Spring Boot"),
                    ProjectStackSummary(firstProjectId, "PostgreSQL"),
                    ProjectStackSummary(secondProjectId, "React"),
                ),
            )

        val response = service.getProjects(page = 0, size = 200, keyword = "  modev  ")

        assertEquals(1, response.pagination.currentPage)
        assertEquals(100, response.pagination.size)
        assertEquals(2, response.pagination.totalCount)
        assertEquals(listOf("Spring Boot", "PostgreSQL"), response.projects[0].stacks)
        assertEquals(listOf("React"), response.projects[1].stacks)
        verify(projectRepository)
            .findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull("modev", pageable)
    }

    @Test
    fun `returns all active projects when keyword is blank`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "modev")
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))

        `when`(projectRepository.findByDeletedAtIsNull(pageable)).thenReturn(PageImpl(listOf(project), pageable, 1))
        `when`(techStackRepository.findByProjectIdIn(listOf(projectId))).thenReturn(emptyList())

        val response = service.getProjects(page = 1, size = 10, keyword = "   ")

        assertEquals(projectId, response.projects.single().projectId)
        assertEquals(emptyList(), response.projects.single().stacks)
        verify(projectRepository).findByDeletedAtIsNull(pageable)
    }
}
