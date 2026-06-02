package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.Dependency
import org.springframework.data.jpa.repository.JpaRepository

interface DependencyRepository : JpaRepository<Dependency, Long> {
    fun findByTechStackPublicIdInOrderByIdAsc(stackIds: Collection<String>): List<Dependency>

    fun findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
        stackIds: Collection<String>,
        keyword: String,
    ): List<Dependency>
}
