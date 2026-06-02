package modeep.modev.domain.catalog.controller

import modeep.modev.domain.catalog.service.GetDependenciesService
import modeep.modev.domain.catalog.service.GetFieldsService
import modeep.modev.domain.catalog.service.GetTechStacksService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog")
class CatalogController(
    private val getDependenciesService: GetDependenciesService,
    private val getFieldsService: GetFieldsService,
    private val getTechStacksService: GetTechStacksService,
) {
    @GetMapping("/fields")
    fun getFields(): ApiResponse =
        ApiResponse(
            success = true,
            data = getFieldsService.execute(),
        )

    @GetMapping("/stacks")
    fun getTechStacks(
        @RequestParam fieldIds: String?,
        @RequestParam(required = false) keyword: String?,
    ): ApiResponse {
        val parsedFieldIds = parseRequiredIds(fieldIds)

        return ApiResponse(
            success = true,
            data = getTechStacksService.execute(parsedFieldIds, keyword),
        )
    }

    @GetMapping("/dependencies")
    fun getDependencies(
        @RequestParam stackIds: String?,
        @RequestParam(required = false) keyword: String?,
    ): ApiResponse {
        val parsedStackIds = parseRequiredIds(stackIds)

        return ApiResponse(
            success = true,
            data = getDependenciesService.execute(parsedStackIds, keyword),
        )
    }

    private fun parseRequiredIds(value: String?): List<String> {
        val ids =
            value
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

        if (ids.isEmpty()) {
            throw BusinessException(GlobalErrorCode.VALIDATION_ERROR)
        }

        return ids
    }
}
