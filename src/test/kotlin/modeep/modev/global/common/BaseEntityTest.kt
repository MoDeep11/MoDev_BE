package modeep.modev.global.common

import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BaseEntityTest {
    @Test
    fun `created at and updated at are initialized as offset date time`() {
        val before = OffsetDateTime.now()

        val entity = BaseEntity()

        val after = OffsetDateTime.now()
        assertIs<OffsetDateTime>(entity.createdAt)
        assertIs<OffsetDateTime>(entity.updatedAt)
        assertTrue(!entity.createdAt.isBefore(before))
        assertTrue(!entity.createdAt.isAfter(after))
        assertTrue(!entity.updatedAt.isBefore(before))
        assertTrue(!entity.updatedAt.isAfter(after))
    }
}
