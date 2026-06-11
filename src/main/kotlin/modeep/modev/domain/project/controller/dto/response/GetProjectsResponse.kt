package modeep.modev.domain.project.controller.dto.response

import java.time.LocalDateTime

data class GetProjectsResponse(
    val projects: List<ProjectSummaryResponse>,
    val pagination: PaginationResponse,
)

data class ProjectSummaryResponse(
    val projectId: String,
    val projectName: String,
    val description: String?,
    val stacks: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: String,
)

data class PaginationResponse(
    val currentPage: Int,
    val totalPages: Int,
    val totalCount: Long,
    val size: Int,
)
