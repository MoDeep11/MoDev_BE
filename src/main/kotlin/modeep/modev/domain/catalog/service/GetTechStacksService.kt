package modeep.modev.domain.catalog.service

import modeep.modev.domain.catalog.controller.dto.response.GetTechStacksResponse
import modeep.modev.domain.catalog.controller.dto.response.TechStackResponse
import modeep.modev.domain.catalog.repository.TechStackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetTechStacksService(
    private val techStackRepository: TechStackRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        fieldIds: List<String>,
        keyword: String?,
    ): GetTechStacksResponse {
        val fieldPublicIds = fieldIds.toSet()
        val stacks =
            if (keyword.isNullOrBlank()) {
                techStackRepository.findStacksByFieldPublicIds(fieldPublicIds)
            } else {
                techStackRepository.findStacksByFieldPublicIdsAndKeyword(fieldPublicIds, keyword)
            }

        return GetTechStacksResponse(
            stacks = stacks.map { TechStackResponse.from(it) },
        )
    }
}
