package modeep.modev.domain.structure.worker

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StructureStatusServiceTest {
    private val projectRepository = mock(ProjectRepository::class.java)
    private val structureFileRepository = mock(StructureFileRepository::class.java)
    private val fieldRepository = mock(FieldRepository::class.java)
    private val techStackRepository = mock(TechStackRepository::class.java)
    private val dependencyRepository = mock(DependencyRepository::class.java)
    private val service =
        StructureStatusService(
            projectRepository = projectRepository,
            structureFileRepository = structureFileRepository,
            fieldRepository = fieldRepository,
            techStackRepository = techStackRepository,
            dependencyRepository = dependencyRepository,
        )

    @Test
    fun `starts generation when project is pending`() {
        val projectId = UUID.randomUUID()
        val project = project(projectId, ProjectStatus.PENDING)
        `when`(projectRepository.findByIdAndDeletedAtIsNullForUpdate(projectId)).thenReturn(project)
        `when`(fieldRepository.findByProjectId(projectId)).thenReturn(emptyList())
        `when`(techStackRepository.findByProjectId(projectId)).thenReturn(emptyList())
        `when`(dependencyRepository.findByProjectId(projectId)).thenReturn(emptyList())

        val result = service.startGeneratingIfPending(projectId)

        assertEquals(ProjectStatus.GENERATING, result.status)
        assertEquals(ProjectStatus.GENERATING, project.status)
        assertNotNull(result.event)
    }

    @Test
    fun `returns generating without publishing duplicate event`() {
        val projectId = UUID.randomUUID()
        val project = project(projectId, ProjectStatus.GENERATING)
        `when`(projectRepository.findByIdAndDeletedAtIsNullForUpdate(projectId)).thenReturn(project)

        val result = service.startGeneratingIfPending(projectId)

        assertEquals(ProjectStatus.GENERATING, result.status)
        assertNull(result.event)
        verifyNoInteractions(fieldRepository, techStackRepository, dependencyRepository, structureFileRepository)
    }

    @Test
    fun `rejects stream start when project is not created`() {
        val projectId = UUID.randomUUID()
        `when`(projectRepository.findByIdAndDeletedAtIsNullForUpdate(projectId))
            .thenReturn(project(projectId, ProjectStatus.NOT_CREATED))

        val exception =
            assertIs<BusinessException>(
                runCatching {
                    service.startGeneratingIfPending(projectId)
                }.exceptionOrNull(),
            )

        assertEquals(ProjectErrorCode.PROJECT_STRUCTURE_NOT_PENDING, exception.errorCode)
    }

    private fun project(
        projectId: UUID,
        status: ProjectStatus,
    ): Project =
        Project(
            id = projectId,
            projectName = "test",
            status = status,
        )
}
