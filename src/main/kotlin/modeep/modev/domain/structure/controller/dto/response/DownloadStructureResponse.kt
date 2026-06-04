package modeep.modev.domain.structure.controller.dto.response

import java.time.OffsetDateTime

data class DownloadStructureResponse(
    val downloadUrl: String,
    val expiresAt: OffsetDateTime,
    val fileName: String,
)
