package modeep.modev.domain.catalog.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.global.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "dependencies")
class Dependency(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "public_id", unique = true, nullable = false, updatable = false, length = 100)
    val publicId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "tech_stack_id",
        foreignKey = ForeignKey(name = "fk_dependency_tech_stack"),
    )
    val techStack: TechStack,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = true)
    val description: String? = null,
    @Column(nullable = true)
    var version: String? = null,
    @Column(nullable = false, name = "is_recommended")
    val isRecommended: Boolean = false,
    @Column(name = "document_url", nullable = true)
    val documentUrl: String? = null,
    @Column(name = "registry_type", nullable = true)
    @Enumerated(EnumType.STRING)
    val registryType: RegistryType? = null,
    @Column(name = "registry_identifier", nullable = true)
    val registryIdentifier: String? = null,
    @Column(name = "registry_auto_sync", nullable = false)
    val registryAutoSync: Boolean = true,
    @Column(name = "registry_last_synced_at", nullable = true)
    var registryLastSyncedAt: Instant? = null,
    @Column(name = "registry_last_sync_error", nullable = true, length = 500)
    var registryLastSyncError: String? = null,
) : BaseEntity() {
    fun recordRegistrySyncSuccess(syncedAt: Instant) {
        this.registryLastSyncedAt = syncedAt
        this.registryLastSyncError = null
    }

    fun recordRegistrySyncFailure(errorMessage: String) {
        this.registryLastSyncError = errorMessage.take(500)
    }
}
