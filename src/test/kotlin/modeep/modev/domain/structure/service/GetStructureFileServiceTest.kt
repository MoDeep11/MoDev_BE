package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.exception.error.StructureErrorCode
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetStructureFileServiceTest {
    private val structureFileRepository = mock(StructureFileRepository::class.java)
    private val service = GetStructureFileService(structureFileRepository)

    @Test
    fun `returns generated file content`() {
        val projectId = UUID.randomUUID()
        `when`(structureFileRepository.findByProjectIdAndPath(projectId, "backend/build.gradle"))
            .thenReturn(
                StructureFile(
                    projectId = projectId,
                    type = StructureFileType.FILE,
                    path = "backend/build.gradle",
                    depth = 1,
                    content = "plugins {}",
                ),
            )

        val response = service.execute(projectId, "backend/build.gradle")

        assertEquals("backend/build.gradle", response.filePath)
        assertEquals("plugins {}", response.content)
        assertEquals("groovy", response.language)
    }

    @Test
    fun `throws file not found when path does not exist`() {
        val exception =
            kotlin.runCatching {
                service.execute(UUID.randomUUID(), "backend/missing.kt")
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(StructureErrorCode.FILE_NOT_FOUND, businessException.errorCode)
    }

    @Test
    fun `throws file not found when path is directory`() {
        val projectId = UUID.randomUUID()
        `when`(structureFileRepository.findByProjectIdAndPath(projectId, "backend"))
            .thenReturn(
                StructureFile(
                    projectId = projectId,
                    type = StructureFileType.DIRECTORY,
                    path = "backend",
                    depth = 0,
                    content = null,
                ),
            )

        val exception =
            kotlin.runCatching {
                service.execute(projectId, "backend")
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(StructureErrorCode.FILE_NOT_FOUND, businessException.errorCode)
    }

    @Test
    fun `throws validation error for unsafe path`() {
        val exception =
            kotlin.runCatching {
                service.execute(UUID.randomUUID(), "../secret.txt")
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(GlobalErrorCode.VALIDATION_ERROR, businessException.errorCode)
    }
}
