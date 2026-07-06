package modeep.modev.domain.catalog.controller

import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.catalog.service.GetDependenciesService
import modeep.modev.domain.catalog.service.GetFieldsService
import modeep.modev.domain.catalog.service.GetTechStacksService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.GlobalErrorCode
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogControllerTest {
    private val dependencyRepository = mock(DependencyRepository::class.java)
    private val fieldRepository = mock(FieldRepository::class.java)
    private val techStackRepository = mock(TechStackRepository::class.java)
    private val controller =
        CatalogController(
            getDependenciesService = GetDependenciesService(dependencyRepository),
            getFieldsService = GetFieldsService(fieldRepository),
            getTechStacksService = GetTechStacksService(techStackRepository),
        )

    @Test
    fun `get fields returns success api response`() {
        `when`(fieldRepository.findAllByOrderByIdAsc())
            .thenReturn(
                listOf(
                    Field(
                        id = 1L,
                        publicId = "field_be",
                        name = "Backend",
                    ),
                ),
            )

        val response = controller.getFields()
        val body = requireNotNull(response.body)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(body.success)
        assertNull(body.error)
        assertIs<Any>(body.data)
    }

    @Test
    fun `get tech stacks parses required field ids`() {
        `when`(techStackRepository.findStacksByFieldPublicIds(setOf("field_be", "field_fe")))
            .thenReturn(emptyList())

        val response = controller.getTechStacks(fieldIds = "field_be, field_fe", keyword = null)
        val body = requireNotNull(response.body)

        verify(techStackRepository).findStacksByFieldPublicIds(setOf("field_be", "field_fe"))
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(body.success)
        assertNull(body.error)
    }

    @Test
    fun `get dependencies parses stack ids and keyword`() {
        `when`(
            dependencyRepository.findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
                stackIds = listOf("stack_spring", "stack_react"),
                keyword = "security",
            ),
        ).thenReturn(emptyList())

        val response = controller.getDependencies(stackIds = "stack_spring, stack_react", keyword = " security ")
        val body = requireNotNull(response.body)

        verify(dependencyRepository)
            .findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
                listOf("stack_spring", "stack_react"),
                "security",
            )
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(body.success)
        assertNull(body.error)
    }

    @Test
    fun `get tech stacks throws validation error when field ids are blank`() {
        val exception =
            kotlin.runCatching {
                controller.getTechStacks(fieldIds = " , ", keyword = null)
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(GlobalErrorCode.VALIDATION_ERROR, businessException.errorCode)
    }

    @Test
    fun `get dependencies throws validation error when stack ids are missing`() {
        val exception =
            kotlin.runCatching {
                controller.getDependencies(stackIds = null, keyword = null)
            }.exceptionOrNull()

        val businessException = assertIs<BusinessException>(exception)
        assertEquals(GlobalErrorCode.VALIDATION_ERROR, businessException.errorCode)
    }
}
