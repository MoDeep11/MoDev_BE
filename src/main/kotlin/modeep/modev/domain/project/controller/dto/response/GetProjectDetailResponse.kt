package modeep.modev.domain.project.controller.dto.response

import java.time.Instant

data class GetProjectDetailResponse(
    val projectId: String,
    val projectName: String,
    val description: String?,
    val fields: List<String>,
    val stacks: List<ProjectStackResponse>,
    val dependencies: List<ProjectDependencyResponse>,
    val fileTree: String,
    val createdAt: Instant,
    val updatedAt: Instant,
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
