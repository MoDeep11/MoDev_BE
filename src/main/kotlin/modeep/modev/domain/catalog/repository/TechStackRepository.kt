package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.TechStack
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TechStackRepository : JpaRepository<TechStack, Long> {
    fun findByPublicIdIn(publicIds: Collection<String>): List<TechStack>

    @Query(
        """
        SELECT ts FROM TechStack ts
        JOIN ProjectTechStack pts ON pts.id.techStackId = ts.id
        WHERE pts.id.projectId = :projectId
        ORDER BY ts.id ASC
        """,
    )
    fun findByProjectId(
        @Param("projectId") projectId: String,
    ): List<TechStack>

    @Query(
        """
        SELECT new modeep.modev.domain.catalog.repository.ProjectStackSummary(pts.id.projectId, ts.name)
        FROM TechStack ts
        JOIN ProjectTechStack pts ON pts.id.techStackId = ts.id
        WHERE pts.id.projectId IN :projectIds
        ORDER BY pts.id.projectId ASC, ts.id ASC
        """,
    )
    fun findByProjectIdIn(
        @Param("projectIds") projectIds: Collection<String>,
    ): List<ProjectStackSummary>

    @Query(
        """
        SELECT ts FROM TechStack ts
        JOIN FieldStackMapping fsm ON fsm.id.techStackId = ts.id
        JOIN Field f ON fsm.id.fieldId = f.id
        WHERE f.publicId IN :fieldPublicIds
    """,
    )
    fun findStacksByFieldPublicIds(
        @Param("fieldPublicIds") fieldPublicIds: Set<String>,
    ): List<TechStack>

    @Query(
        """
        SELECT ts.* FROM tech_stacks ts
        JOIN field_stack_mappings fsm ON fsm.tech_stack_id = ts.id
        JOIN fields f ON fsm.field_id = f.id
        WHERE f.public_id = ANY(ARRAY[:fieldPublicIds])
        AND to_tsvector('simple', ts.name) @@ plainto_tsquery('simple', :keyword)
    """,
        nativeQuery = true,
    )
    fun findStacksByFieldPublicIdsAndKeyword(
        @Param("fieldPublicIds") fieldPublicIds: Set<String>,
        @Param("keyword") keyword: String,
    ): List<TechStack>
}

data class ProjectStackSummary(
    val projectId: String,
    val name: String,
)
