package modeep.modev.domain.project.controller.dto.response

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class GetProjectDetailResponse(
    val projectId: String,
    val projectName: String,
    val description: String?,
    val fields: List<String>,
    val stacks: List<ProjectStackResponse>,
    val dependencies: List<ProjectDependencyResponse>,
    val fileTree: JsonNode,
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
