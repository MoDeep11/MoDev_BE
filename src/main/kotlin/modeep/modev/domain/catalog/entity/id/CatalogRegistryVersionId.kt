package modeep.modev.domain.catalog.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import java.io.Serializable

@Embeddable
data class CatalogRegistryVersionId(
    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val targetType: CatalogRegistryTargetType,
    @Column(name = "target_id", nullable = false)
    val targetId: Long,
    @Column(nullable = false)
    val version: String,
) : Serializable
