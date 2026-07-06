package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.request.UpdateProjectStacksRequest
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectDependencyRepository
import modeep.modev.domain.project.repository.ProjectFieldRepository
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.project.repository.ProjectTechStackRepository
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.controller.dto.response.GenerateStructureResponse
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.domain.structure.service.GenerateStructureService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals

class UpdateProjectStacksServiceTest {
    private val userId = 1L
    private lateinit var projectRepository: ProjectRepository
    private lateinit var fieldRepository: FieldRepository
    private lateinit var techStackRepository: TechStackRepository
    private lateinit var dependencyRepository: DependencyRepository
    private lateinit var projectFieldRepository: ProjectFieldRepository
    private lateinit var projectTechStackRepository: ProjectTechStackRepository
    private lateinit var projectDependencyRepository: ProjectDependencyRepository
    private lateinit var structureFileRepository: StructureFileRepository
    private lateinit var generateStructureService: GenerateStructureService
    private lateinit var service: UpdateProjectStacksService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        fieldRepository = mock(FieldRepository::class.java)
        techStackRepository = mock(TechStackRepository::class.java)
        dependencyRepository = mock(DependencyRepository::class.java)
        projectFieldRepository = mock(ProjectFieldRepository::class.java)
        projectTechStackRepository = mock(ProjectTechStackRepository::class.java)
        projectDependencyRepository = mock(ProjectDependencyRepository::class.java)
        structureFileRepository = mock(StructureFileRepository::class.java)
        generateStructureService = mock(GenerateStructureService::class.java)
        service =
            UpdateProjectStacksService(
                projectRepository = projectRepository,
                fieldRepository = fieldRepository,
                techStackRepository = techStackRepository,
                dependencyRepository = dependencyRepository,
                projectFieldRepository = projectFieldRepository,
                projectTechStackRepository = projectTechStackRepository,
                projectDependencyRepository = projectDependencyRepository,
                structureFileRepository = structureFileRepository,
                generateStructureService = generateStructureService,
            )
    }

    @Test
    fun `updates selected catalogs and requests ai regeneration`() {
        val projectId = UUID.randomUUID()
        val backend = Field(id = 1L, publicId = "domain_be", name = "Backend")
        val frontend = Field(id = 2L, publicId = "domain_fe", name = "Frontend")
        val spring =
            TechStack(
                id = 1L,
                publicId = "stack_spring",
                name = "Spring Boot",
                version = "3.5.0",
                category = Category.FRAMEWORK,
                field = backend,
            )
        val react =
            TechStack(
                id = 2L,
                publicId = "stack_react",
                name = "React",
                version = "19",
                category = Category.FRAMEWORK,
                field = frontend,
            )
        val redis =
            TechStack(
                id = 3L,
                publicId = "stack_redis",
                name = "Redis",
                version = "8",
                category = Category.DATABASE,
                field = backend,
            )
        val security = dependency(1L, "dep_spring_security", "Spring Security", spring)
        val jpa = dependency(2L, "dep_jpa", "Spring Data JPA", spring)
        val request =
            UpdateProjectStacksRequest(
                fieldIds = listOf("domain_fe", "domain_be"),
                stackIds = listOf("stack_spring", "stack_react", "stack_redis"),
                dependencyIds = listOf("dep_spring_security", "dep_jpa"),
            )

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId))
            .thenReturn(Project(id = projectId, userId = userId, projectName = "project"))
        `when`(fieldRepository.findByPublicIdIn(request.fieldIds)).thenReturn(listOf(frontend, backend))
        `when`(techStackRepository.findByPublicIdIn(request.stackIds)).thenReturn(listOf(spring, react, redis))
        `when`(dependencyRepository.findByPublicIdIn(request.dependencyIds)).thenReturn(listOf(security, jpa))
        `when`(techStackRepository.findStacksByFieldPublicIds(setOf("domain_fe", "domain_be")))
            .thenReturn(listOf(spring, react, redis))
        `when`(
            dependencyRepository.findByTechStackPublicIdInOrderByIdAsc(
                setOf("stack_spring", "stack_react", "stack_redis"),
            ),
        ).thenReturn(listOf(security, jpa))
        `when`(generateStructureService.execute(GenerateStructureRequest(projectId)))
            .thenReturn(GenerateStructureResponse(projectId, ProjectStatus.PENDING))

        val response = service.execute(projectId, userId, request)

        assertEquals(projectId, response.projectId)
        assertEquals(ProjectStatus.PENDING, response.status)
        verify(projectFieldRepository).deleteAllByIdProjectId(projectId)
        verify(projectTechStackRepository).deleteAllByIdProjectId(projectId)
        verify(projectDependencyRepository).deleteAllByIdProjectId(projectId)
        verify(structureFileRepository).deleteAllByProjectId(projectId)
        verify(generateStructureService).execute(GenerateStructureRequest(projectId))
    }

    private fun dependency(
        id: Long,
        publicId: String,
        name: String,
        stack: TechStack,
    ) = Dependency(
        id = id,
        publicId = publicId,
        techStack = stack,
        name = name,
        version = "1.0",
    )
}
