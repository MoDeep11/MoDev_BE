package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.ProjectStore
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetStructureStatusServiceTest {
    private val projectStore = ProjectStore()
    private val structureFileRepository = mock(StructureFileRepository::class.java)
    private val service =
        GetStructureStatusService(
            projectStore = projectStore,
            structureFileRepository = structureFileRepository,
        )

    @Test
    fun `returns pending status without result`() {
        val projectId = UUID.randomUUID()

        val response = service.execute(projectId)

        assertEquals(projectId.toString(), response.projectId)
        assertEquals("PENDING", response.status)
        assertNull(response.result)
        verifyNoInteractions(structureFileRepository)
    }

    @Test
    fun `returns completed status with rebuilt file tree`() {
        val projectId = UUID.randomUUID()
        projectStore.updateStatus(projectId, "COMPLETED")
        `when`(structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId))
            .thenReturn(
                listOf(
                    StructureFile(
                        projectId = projectId,
                        type = StructureFileType.FILE,
                        path = "backend/src/main/Application.kt",
                        depth = 3,
                        content = "fun main() {}",
                    ),
                    StructureFile(
                        projectId = projectId,
                        type = StructureFileType.FILE,
                        path = "README.md",
                        depth = 0,
                        content = "# test",
                    ),
                ),
            )

        val response = service.execute(projectId)

        assertEquals("COMPLETED", response.status)
        val fileTree = assertNotNull(response.result).fileTree
        assertEquals(listOf("backend", "README.md"), fileTree.map { it.name })
        assertEquals("DIRECTORY", fileTree[0].type)
        assertEquals("src", fileTree[0].children.single().name)
        assertEquals("main", fileTree[0].children.single().children.single().name)
        assertEquals("Application.kt", fileTree[0].children.single().children.single().children.single().name)
        assertEquals("FILE", fileTree[1].type)
    }
}
