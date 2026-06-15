package modeep.modev.domain.structure.service

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import modeep.modev.domain.structure.ProjectStore
import modeep.modev.domain.structure.controller.dto.response.DownloadStructureResponse
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.config.properties.S3Properties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class DownloadStructureService(
    private val projectStore: ProjectStore,
    private val structureFileRepository: StructureFileRepository,
    private val s3Template: S3Template,
    private val s3Properties: S3Properties,
) {
    @Transactional(readOnly = true)
    fun issueDownloadUrl(projectId: UUID): DownloadStructureResponse {
        val project =
            projectStore.get(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val expiration = Duration.ofHours(1)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(expiration)
        val fileName = "${project.name.toSafeFileName()}_${DateTimeFormatter.BASIC_ISO_DATE.format(expiresAt.toLocalDate())}.zip"
        val objectKey = "$projectId/structures/downloads/$fileName"
        val zip = createZip(projectId)

        uploadZip(
            objectKey = objectKey,
            fileName = fileName,
            zip = zip,
        )

        return DownloadStructureResponse(
            downloadUrl = createPresignedUrl(objectKey, expiration),
            expiresAt = expiresAt,
            fileName = fileName,
        )
    }

    // zip 파일 생성
    private fun createZip(projectId: UUID): ByteArray {
        projectStore.get(projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val files = structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId)
        if (files.isEmpty()) {
            throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        }

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            files.forEach { file ->
                val entryName = file.path.trim('/')
                if (entryName.isBlank()) {
                    return@forEach
                }

                val zipEntryName =
                    if (file.type == StructureFileType.DIRECTORY) {
                        "$entryName/"
                    } else {
                        entryName
                    }

                zip.putNextEntry(ZipEntry(zipEntryName))
                if (file.type == StructureFileType.FILE) {
                    zip.write(file.content.orEmpty().toByteArray(Charsets.UTF_8))
                }
                zip.closeEntry()
            }
        }

        return output.toByteArray()
    }

    // S3에 zip 파일 업로드
    private fun uploadZip(
        objectKey: String,
        fileName: String,
        zip: ByteArray,
    ) {
        val request =
            ObjectMetadata.builder()
                .contentType("application/zip")
                .contentDisposition("attachment; filename=\"$fileName\"")
                .contentLength(zip.size.toLong())
                .build()

        s3Template.upload(s3Properties.bucket, objectKey, ByteArrayInputStream(zip), request)
    }

    // presignal url 발급
    private fun createPresignedUrl(
        objectKey: String,
        expiration: Duration,
    ): String =
        s3Template.createSignedGetURL(s3Properties.bucket, objectKey, expiration)
            .toString()

    // 세이프 파일 명
    private fun String.toSafeFileName(): String =
        trim()
            .replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-', '.', '_')
            .ifBlank { "project" }
}
