package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.entity.vo.Ecosystem
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.repository.ProjectDependencyRepository
import modeep.modev.domain.project.repository.ProjectFieldRepository
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.project.repository.ProjectTechStackRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PostProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var fieldRepository: FieldRepository
    private lateinit var dependencyRepository: DependencyRepository
    private lateinit var techStackRepository: TechStackRepository
    private lateinit var projectFieldRepository: ProjectFieldRepository
    private lateinit var projectTechStackRepository: ProjectTechStackRepository
    private lateinit var projectDependencyRepository: ProjectDependencyRepository
    private lateinit var postProjectService: PostProjectService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        fieldRepository = mock(FieldRepository::class.java)
        dependencyRepository = mock(DependencyRepository::class.java)
        techStackRepository = mock(TechStackRepository::class.java)
        projectFieldRepository = mock(ProjectFieldRepository::class.java)
        projectTechStackRepository = mock(ProjectTechStackRepository::class.java)
        projectDependencyRepository = mock(ProjectDependencyRepository::class.java)
        postProjectService =
            PostProjectService(
                projectRepository = projectRepository,
                fieldRepository = fieldRepository,
                dependencyRepository = dependencyRepository,
                techStackRepository = techStackRepository,
                projectFieldRepository = projectFieldRepository,
                projectTechStackRepository = projectTechStackRepository,
                projectDependencyRepository = projectDependencyRepository,
            )
    }

    @Test
    fun `rejects a stack that is not mapped to the selected field`() {
        val field = Field(id = 1L, publicId = "backend", name = "Backend")
        val stack =
            TechStack(
                id = 1L,
                publicId = "react",
                name = "React",
                ecosystem = Ecosystem.NODE,
                category = Category.FRAMEWORK,
                field = field,
            )
        val request =
            SaveProjectRequest(
                projectName = "project",
                fieldIds = listOf(field.publicId),
                stackIds = listOf(stack.publicId),
            )
        `when`(fieldRepository.findByPublicIdIn(request.fieldIds)).thenReturn(listOf(field))
        `when`(techStackRepository.findByPublicIdIn(request.stackIds)).thenReturn(listOf(stack))
        `when`(dependencyRepository.findByPublicIdIn(request.dependencyIds)).thenReturn(emptyList())
        `when`(techStackRepository.findStacksByFieldPublicIds(setOf(field.publicId))).thenReturn(emptyList())

        val exception =
            assertFailsWith<BusinessException> {
                postProjectService.saveProject(request)
            }

        assertEquals(ProjectErrorCode.INVALID_STACK_COMBINATION, exception.errorCode)
        assertEquals(mapOf("stackIds" to setOf(stack.publicId)), exception.details)
        verifyNoInteractions(
            projectRepository,
            projectFieldRepository,
            projectTechStackRepository,
            projectDependencyRepository,
        )
    }
}
