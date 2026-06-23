package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.CatalogRegistryVersion
import modeep.modev.domain.catalog.entity.id.CatalogRegistryVersionId
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CatalogRegistryVersionRepository : JpaRepository<CatalogRegistryVersion, CatalogRegistryVersionId> {
    fun findByIdTargetTypeAndIdTargetId(
        targetType: CatalogRegistryTargetType,
        targetId: Long,
    ): List<CatalogRegistryVersion>

    @Query(
        """
        SELECT crv
        FROM CatalogRegistryVersion crv
        WHERE crv.id.targetType = :targetType
        AND crv.id.targetId IN :targetIds
        AND crv.isLatest = true
        """,
    )
    fun findLatestByTargetTypeAndTargetIdIn(
        @Param("targetType") targetType: CatalogRegistryTargetType,
        @Param("targetIds") targetIds: Collection<Long>,
    ): List<CatalogRegistryVersion>
}
