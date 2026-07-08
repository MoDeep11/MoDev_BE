package modeep.modev.domain.structure.service

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.entity.validateOwner
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.response.DownloadStructureResponse
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import modeep.modev.global.storage.S3StorageService
import modeep.modev.global.zip.ZipArchiveEntry
import modeep.modev.global.zip.ZipArchiveEntryType
import modeep.modev.global.zip.ZipArchiveService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class DownloadStructureService(
    private val projectRepository: ProjectRepository,
    private val structureFileRepository: StructureFileRepository,
    private val zipArchiveService: ZipArchiveService,
    private val s3StorageService: S3StorageService,
) {
    @Transactional(readOnly = true)
    fun issueDownloadUrl(
        projectId: UUID,
        userId: Long? = null,
    ): DownloadStructureResponse {
        val zip = createDownloadZip(projectId, userId)
        val expiration = Duration.ofHours(1)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(expiration)
        val objectKey = "$projectId/structures/downloads/${zip.fileName}"

        s3StorageService.upload(
            objectKey = objectKey,
            fileName = zip.fileName,
            contentType = ZIP_CONTENT_TYPE,
            content = zip.content,
        )

        return DownloadStructureResponse(
            downloadUrl = s3StorageService.createPresignedGetUrl(objectKey, expiration),
            expiresAt = expiresAt,
            fileName = zip.fileName,
        )
    }

    @Transactional(readOnly = true)
    fun issueDirectDownloadUrl(
        projectId: UUID,
        downloadUrl: String,
        userId: Long? = null,
    ): DownloadStructureResponse {
        val zip = createDownloadZip(projectId, userId)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofHours(1))

        return DownloadStructureResponse(
            downloadUrl = downloadUrl,
            expiresAt = expiresAt,
            fileName = zip.fileName,
        )
    }

    @Transactional(readOnly = true)
    fun createDownloadZip(
        projectId: UUID,
        userId: Long? = null,
    ): StructureZip {
        val project = findByProjectId(projectId)
        userId?.let(project::validateOwner)

        val fileName =
            "${project.projectName.toSafeFileName()}_${DateTimeFormatter.BASIC_ISO_DATE.format(
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDate(),
            )}.zip"

        return StructureZip(
            fileName = fileName,
            content = createZip(projectId),
        )
    }

    private fun findByProjectId(projectId: UUID): Project {
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        return if (project.status == ProjectStatus.COMPLETED) {
            project
        } else {
            throw BusinessException(ProjectErrorCode.PROJECT_NOT_COMPLETED)
        }
    }

    private fun createZip(projectId: UUID): ByteArray {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val files = structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId)
        if (files.isEmpty()) {
            throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        }

        return zipArchiveService.create(
            files.map {
                ZipArchiveEntry(
                    path = it.path,
                    type =
                        if (it.type == StructureFileType.DIRECTORY) {
                            ZipArchiveEntryType.DIRECTORY
                        } else {
                            ZipArchiveEntryType.FILE
                        },
                    content = it.content,
                )
            },
        )
    }

    private fun String.toSafeFileName(): String =
        trim()
            .replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-', '.', '_')
            .ifBlank { "project" }

    private companion object {
        const val ZIP_CONTENT_TYPE = "application/zip"
    }
}

data class StructureZip(
    val fileName: String,
    val content: ByteArray,
)
