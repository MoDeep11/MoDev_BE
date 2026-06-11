package modeep.modev.domain.project.controller.dto.response

import java.time.LocalDateTime

data class GetProjectDetailResponse(
    val projectId: String,
    val projectName: String,
    val description: String?,
    val fields: List<String>,
    val stacks: List<ProjectStackResponse>,
    val dependencies: List<ProjectDependencyResponse>,
    val fileTree: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ProjectStackResponse(
    val stackId: String,
    val name: String,
    val category: String,
)

data class ProjectDependencyResponse(
    val dependencyId: String,
    val name: String,
    val version: String?,
    val stackId: String,
)
