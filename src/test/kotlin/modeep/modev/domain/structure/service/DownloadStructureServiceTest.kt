package modeep.modev.domain.structure.service

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import modeep.modev.domain.structure.ProjectStore
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.config.properties.S3Properties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import modeep.modev.global.storage.S3StorageService
import modeep.modev.global.zip.ZipArchiveService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.InputStream
import java.net.URI
import java.time.Duration
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DownloadStructureServiceTest {
    private val projectStore = ProjectStore()
    private val structureFileRepository = mock(StructureFileRepository::class.java)
    private val s3Template = mock(S3Template::class.java)
    private val service =
        DownloadStructureService(
            projectStore = projectStore,
            structureFileRepository = structureFileRepository,
            zipArchiveService = ZipArchiveService(),
            s3StorageService =
                S3StorageService(
                    s3Template = s3Template,
                    s3Properties = S3Properties(bucket = "test-bucket"),
                ),
        )

    @Test
    fun `uploads generated project zip and returns presigned url`() {
        val projectId = UUID.randomUUID()
        val inputCaptor = ArgumentCaptor.forClass(InputStream::class.java)
        val metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata::class.java)
        `when`(structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId))
            .thenReturn(
                listOf(
                    StructureFile(
                        projectId = projectId,
                        type = StructureFileType.DIRECTORY,
                        path = "backend",
                        depth = 0,
                    ),
                    StructureFile(
                        projectId = projectId,
                        type = StructureFileType.FILE,
                        path = "backend/build.gradle",
                        depth = 1,
                        content = "plugins {}",
                    ),
                ),
            )
        `when`(
            s3Template.createSignedGetURL(
                eq("test-bucket"),
                anyString(),
                any(Duration::class.java),
            ),
        ).thenReturn(URI("https://storage.example.com/test.zip?token=abc").toURL())

        val response = service.issueDownloadUrl(projectId)

        verify(s3Template).upload(
            eq("test-bucket"),
            eq(response.fileName.toObjectKey(projectId)),
            inputCaptor.capture(),
            metadataCaptor.capture(),
        )
        assertEquals("https://storage.example.com/test.zip?token=abc", response.downloadUrl)
        assertEquals("application/zip", metadataCaptor.value.contentType)
        assertEquals("attachment; filename=\"${response.fileName}\"", metadataCaptor.value.contentDisposition)
        assertTrue(response.fileName.startsWith("test_"))
        assertZipContains(
            zip = inputCaptor.value.readBytes(),
            path = "backend/build.gradle",
            content = "plugins {}",
        )
    }

    @Test
    fun `throws project not found when no structure files exist`() {
        val projectId = UUID.randomUUID()
        `when`(structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId))
            .thenReturn(emptyList())

        val exception =
            kotlin.runCatching {
                service.issueDownloadUrl(projectId)
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, businessException.errorCode)
    }

    private fun String.toObjectKey(projectId: UUID): String = "$projectId/structures/downloads/$this"

    private fun assertZipContains(
        zip: ByteArray,
        path: String,
        content: String,
    ) {
        ZipInputStream(zip.inputStream()).use { input ->
            var entry = input.nextEntry
            while (entry != null) {
                if (entry.name == path) {
                    assertEquals(content, input.readBytes().toString(Charsets.UTF_8))
                    return
                }
                input.closeEntry()
                entry = input.nextEntry
            }
        }

        throw AssertionError("Zip entry not found: $path")
    }
}
