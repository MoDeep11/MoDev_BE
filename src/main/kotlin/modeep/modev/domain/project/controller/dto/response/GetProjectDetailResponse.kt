package modeep.modev.domain.project.controller.dto.response

import modeep.modev.domain.structure.controller.dto.response.GetStructureStatusResponse
import java.time.Instant
import java.util.UUID

data class GetProjectDetailResponse(
    val projectId: UUID,
    val projectName: String,
    val description: String?,
    val fields: List<String>,
    val stacks: List<ProjectStackResponse>,
    val dependencies: List<ProjectDependencyResponse>,
    val fileTree: GetStructureStatusResponse,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ProjectStackResponse(
    val stackId: String,
    val name: String,
    val version: String?,
    val category: String,
)

data class ProjectDependencyResponse(
    val dependencyId: String,
    val name: String,
    val version: String?,
    val stackId: String,
)
