package modeep.modev.domain.catalog.controller.dto.response

import modeep.modev.domain.catalog.entity.TechStack

data class TechStackResponse(
    val stackId: String,
    val fieldId: String,
    val category: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
) {
    companion object {
        fun from(techStack: TechStack) =
            TechStackResponse(
                stackId = techStack.publicId,
                fieldId = techStack.field.publicId,
                category = techStack.category.name,
                name = techStack.name,
                description = techStack.description,
                iconUrl = techStack.iconUrl,
            )
    }
}
