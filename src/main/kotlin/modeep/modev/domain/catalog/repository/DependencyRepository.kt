package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.Dependency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DependencyRepository : JpaRepository<Dependency, Long> {
    fun findByPublicIdIn(publicIds: Collection<String>): List<Dependency>

    @Query(
        """
        SELECT d FROM Dependency d
        JOIN ProjectDependency pd ON pd.id.dependencyId = d.id
        WHERE pd.id.projectId = :projectId
        ORDER BY d.id ASC
        """,
    )
    fun findByProjectId(
        @Param("projectId") projectId: String,
    ): List<Dependency>

    fun findByTechStackPublicIdInOrderByIdAsc(stackIds: Collection<String>): List<Dependency>

    fun findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
        stackIds: Collection<String>,
        keyword: String,
    ): List<Dependency>
}
