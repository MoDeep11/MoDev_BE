package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.Field
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FieldRepository : JpaRepository<Field, Long> {
    fun findAllByOrderByIdAsc(): List<Field>

    fun findByPublicIdIn(publicIds: Collection<String>): List<Field>

    @Query(
        """
        SELECT f FROM Field f
        JOIN ProjectField pf ON pf.id.fieldId = f.id
        WHERE pf.id.projectId = :projectId
        ORDER BY f.id ASC
        """,
    )
    fun findByProjectId(
        @Param("projectId") projectId: String,
    ): List<Field>
}
