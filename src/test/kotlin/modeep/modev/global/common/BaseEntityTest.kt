package modeep.modev.global.common

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BaseEntityTest {
    @Test
    fun `created at and updated at are initialized as instant`() {
        val before = Instant.now()

        val entity = object : BaseEntity() {}

        val after = Instant.now()
        val createdAt = assertIs<Instant>(entity.createdAt)
        val updatedAt = assertIs<Instant>(entity.updatedAt)
        assertTrue(!createdAt.isBefore(before))
        assertTrue(!createdAt.isAfter(after))
        assertTrue(!updatedAt.isBefore(before))
        assertTrue(!updatedAt.isAfter(after))
    }
}
