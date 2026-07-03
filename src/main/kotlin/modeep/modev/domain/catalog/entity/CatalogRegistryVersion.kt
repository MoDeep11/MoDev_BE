package modeep.modev.domain.catalog.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.catalog.entity.id.CatalogRegistryVersionId
import modeep.modev.global.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "catalog_registry_versions")
class CatalogRegistryVersion(
    @EmbeddedId
    val id: CatalogRegistryVersionId,
    @Column(name = "is_latest", nullable = false)
    var isLatest: Boolean = false,
    @Column(name = "is_stable", nullable = false)
    val isStable: Boolean = true,
    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: Instant,
) : BaseEntity() {
    val targetId: Long
        get() = id.targetId

    val version: String
        get() = id.version

    fun markLatest(
        latest: Boolean,
        fetchedAt: Instant,
    ) {
        this.isLatest = latest
        this.fetchedAt = fetchedAt
    }
}
