package modeep.modev.domain.structure.service

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
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
    fun issueDownloadUrl(projectId: UUID): DownloadStructureResponse {
        val project = findByProjectId(projectId)

        val expiration = Duration.ofHours(1)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(expiration)
        val fileName = "${project.projectName.toSafeFileName()}_${DateTimeFormatter.BASIC_ISO_DATE.format(expiresAt.toLocalDate())}.zip"
        val objectKey = "$projectId/structures/downloads/$fileName"
        val zip = createZip(projectId)

        s3StorageService.upload(
            objectKey = objectKey,
            fileName = fileName,
            contentType = ZIP_CONTENT_TYPE,
            content = zip,
        )

        return DownloadStructureResponse(
            downloadUrl = s3StorageService.createPresignedGetUrl(objectKey, expiration),
            expiresAt = expiresAt,
            fileName = fileName,
        )
    }

    fun findByProjectId(projectId: UUID): Project {
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId.toString())
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        return if (project.status == ProjectStatus.COMPLETED) {
            project
        } else {
            throw BusinessException(ProjectErrorCode.PROJECT_NOT_COMPLETED)
        }
    }

    private fun createZip(projectId: UUID): ByteArray {
        projectRepository.findByIdAndDeletedAtIsNull(projectId.toString())
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
