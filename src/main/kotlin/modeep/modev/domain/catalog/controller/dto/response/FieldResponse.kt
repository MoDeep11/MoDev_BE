package modeep.modev.domain.catalog.controller.dto.response

import modeep.modev.domain.catalog.entity.Field

data class FieldResponse(
    val fieldId: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
) {
    companion object {
        fun from(field: Field) =
            FieldResponse(
                fieldId = field.publicId,
                name = field.name,
                description = field.description,
                iconUrl = field.iconUrl,
            )
    }
}
