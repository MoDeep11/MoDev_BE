package modeep.modev.domain.catalog.service

import modeep.modev.domain.catalog.controller.dto.response.FieldResponse
import modeep.modev.domain.catalog.controller.dto.response.GetFieldsResponse
import modeep.modev.domain.catalog.repository.FieldRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetFieldsService(
    private val fieldRepository: FieldRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): GetFieldsResponse =
        GetFieldsResponse(
            fields =
                fieldRepository.findAllByOrderByIdAsc()
                    .map {
                        FieldResponse.from(it)
                    },
        )
}
