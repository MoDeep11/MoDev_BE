package modeep.modev.domain.catalog.service

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class CatalogServiceTest {
    @Test
    fun `get fields maps repository results to response`() {
        val fieldRepository = mock(FieldRepository::class.java)
        val service = GetFieldsService(fieldRepository)
        val field =
            Field(
                id = 1L,
                publicId = "field_be",
                name = "Backend",
                description = "server api",
                iconUrl = "https://cdn.example.com/backend.svg",
            )

        `when`(fieldRepository.findAllByOrderByIdAsc()).thenReturn(listOf(field))

        val response = service.execute()

        assertEquals(1, response.fields.size)
        assertEquals("field_be", response.fields[0].fieldId)
        assertEquals("Backend", response.fields[0].name)
        assertEquals("server api", response.fields[0].description)
        assertEquals("https://cdn.example.com/backend.svg", response.fields[0].iconUrl)
    }

    @Test
    fun `get tech stacks filters by field ids without keyword`() {
        val techStackRepository = mock(TechStackRepository::class.java)
        val service = GetTechStacksService(techStackRepository)
        val techStack = techStack()

        `when`(techStackRepository.findStacksByFieldPublicIds(setOf("field_be"))).thenReturn(listOf(techStack))

        val response = service.execute(fieldIds = listOf("field_be"), keyword = null)

        verify(techStackRepository).findStacksByFieldPublicIds(setOf("field_be"))
        assertEquals(1, response.stacks.size)
        assertEquals("stack_spring", response.stacks[0].stackId)
        assertEquals("field_be", response.stacks[0].fieldId)
        assertEquals("FRAMEWORK", response.stacks[0].category)
        assertEquals("Spring Boot", response.stacks[0].name)
        assertEquals("https://cdn.example.com/spring.svg", response.stacks[0].iconUrl)
    }

    @Test
    fun `get tech stacks filters by field ids and keyword`() {
        val techStackRepository = mock(TechStackRepository::class.java)
        val service = GetTechStacksService(techStackRepository)

        `when`(
            techStackRepository.findStacksByFieldPublicIdsAndKeyword(
                fieldPublicIds = setOf("field_be", "field_fe"),
                keyword = "spring",
            ),
        ).thenReturn(emptyList())

        val response = service.execute(fieldIds = listOf("field_be", "field_fe"), keyword = "spring")

        verify(techStackRepository).findStacksByFieldPublicIdsAndKeyword(setOf("field_be", "field_fe"), "spring")
        assertEquals(emptyList(), response.stacks)
    }

    @Test
    fun `get dependencies maps repository results to response`() {
        val dependencyRepository = mock(DependencyRepository::class.java)
        val service = GetDependenciesService(dependencyRepository)
        val dependency =
            Dependency(
                id = 1L,
                publicId = "dep_spring_security",
                techStack = techStack(),
                name = "Spring Security",
                description = "security library",
                version = "6.2.1",
                isRecommended = true,
                documentUrl = "https://docs.spring.io/spring-security",
            )

        `when`(dependencyRepository.findByTechStackPublicIdInOrderByIdAsc(listOf("stack_spring")))
            .thenReturn(listOf(dependency))

        val response = service.execute(stackIds = listOf("stack_spring"), keyword = null)

        assertEquals(1, response.dependencies.size)
        assertEquals("dep_spring_security", response.dependencies[0].dependencyId)
        assertEquals("stack_spring", response.dependencies[0].stackId)
        assertEquals("Spring Security", response.dependencies[0].name)
        assertEquals("6.2.1", response.dependencies[0].version)
        assertEquals("security library", response.dependencies[0].description)
        assertEquals(true, response.dependencies[0].isRecommended)
        assertEquals("https://docs.spring.io/spring-security", response.dependencies[0].documentUrl)
    }

    @Test
    fun `get dependencies trims keyword before querying`() {
        val dependencyRepository = mock(DependencyRepository::class.java)
        val service = GetDependenciesService(dependencyRepository)

        `when`(
            dependencyRepository.findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
                stackIds = listOf("stack_spring"),
                keyword = "security",
            ),
        ).thenReturn(emptyList())

        val response = service.execute(stackIds = listOf("stack_spring"), keyword = " security ")

        verify(dependencyRepository)
            .findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(listOf("stack_spring"), "security")
        assertEquals(emptyList(), response.dependencies)
    }

    private fun techStack() =
        TechStack(
            id = 1L,
            publicId = "stack_spring",
            name = "Spring Boot",
            description = "backend framework",
            version = "3.2.1",
            category = Category.FRAMEWORK,
            field =
                Field(
                    id = 1L,
                    publicId = "field_be",
                    name = "Backend",
                ),
            iconUrl = "https://cdn.example.com/spring.svg",
        )
}
