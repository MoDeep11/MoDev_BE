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
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID
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
    fun `gets project detail with catalogs and file tree`() {
        val projectId = UUID.randomUUID()
        val field = Field(id = 1L, publicId = "domain_be", name = "Backend")
        val stack =
            TechStack(
                id = 1L,
                publicId = "stack_spring",
                name = "Spring Boot",
                version = "3.5.0",
                category = Category.FRAMEWORK,
                field = field,
            )
        val dependency =
            Dependency(
                id = 1L,
                publicId = "dep_security",
                techStack = stack,
                name = "Spring Security",
                version = "6.5.0",
            )
        val fileTree = GetStructureStatusResponse(projectId, "COMPLETED", null)
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId))
            .thenReturn(Project(id = projectId, projectName = "project", description = "description"))
        `when`(fieldRepository.findByProjectId(projectId)).thenReturn(listOf(field))
        `when`(techStackRepository.findByProjectId(projectId)).thenReturn(listOf(stack))
        `when`(dependencyRepository.findByProjectId(projectId)).thenReturn(listOf(dependency))
        `when`(getStructureStatusService.execute(projectId)).thenReturn(fileTree)

        val response = service.getProjectDetail(projectId)

        assertEquals(projectId, response.projectId)
        assertEquals("project", response.projectName)
        assertEquals("description", response.description)
        assertEquals(listOf("Backend"), response.fields)
        assertEquals("stack_spring", response.stacks.single().stackId)
        assertEquals("Spring Boot", response.stacks.single().name)
        assertEquals(Category.FRAMEWORK.name, response.stacks.single().category)
        assertEquals("dep_security", response.dependencies.single().dependencyId)
        assertEquals("Spring Security", response.dependencies.single().name)
        assertEquals("6.5.0", response.dependencies.single().version)
        assertEquals("stack_spring", response.dependencies.single().stackId)
        assertEquals(fileTree, response.fileTree)
    }

    @Test
    fun `throws when getting detail for missing project`() {
        val projectId = UUID.randomUUID()
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.getProjectDetail(projectId)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `gets paged project list with normalized page size and stacks`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "project", description = "description")
        val pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
        `when`(projectRepository.findByDeletedAtIsNull(pageable))
            .thenReturn(PageImpl(listOf(project), pageable, 1))
        `when`(techStackRepository.findByProjectIdIn(listOf(projectId)))
            .thenReturn(listOf(ProjectStackSummary(projectId, "Spring Boot")))

        val response = service.getProjects(page = 0, size = 200, keyword = "   ")

        assertEquals(1, response.pagination.currentPage)
        assertEquals(100, response.pagination.size)
        assertEquals(1, response.pagination.totalPages)
        assertEquals(1, response.pagination.totalCount)
        assertEquals(projectId, response.projects.single().projectId)
        assertEquals("project", response.projects.single().projectName)
        assertEquals(listOf("Spring Boot"), response.projects.single().stacks)
        verify(projectRepository).findByDeletedAtIsNull(pageable)
    }

    @Test
    fun `gets project list by trimmed keyword`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        `when`(projectRepository.findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull("spring", pageable))
            .thenReturn(PageImpl(emptyList(), pageable, 0))

        val response = service.getProjects(page = 1, size = 20, keyword = "  spring  ")

        assertEquals(emptyList(), response.projects)
        assertEquals(1, response.pagination.currentPage)
        verify(projectRepository).findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull("spring", pageable)
    }
}
