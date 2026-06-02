package modeep.modev.domain.catalog.controller.dto.response

import modeep.modev.domain.catalog.entity.Dependency

data class DependencyResponse(
    val dependencyId: String,
    val stackId: String,
    val name: String,
    val version: String?,
    val description: String?,
    val isRecommended: Boolean,
    val documentUrl: String?,
) {
    companion object {
        fun from(dependency: Dependency) =
            DependencyResponse(
                dependencyId = dependency.publicId,
                stackId = dependency.techStack.publicId,
                name = dependency.name,
                version = dependency.version,
                description = dependency.description,
                isRecommended = dependency.isRecommended,
                documentUrl = dependency.documentUrl,
            )
    }
}
