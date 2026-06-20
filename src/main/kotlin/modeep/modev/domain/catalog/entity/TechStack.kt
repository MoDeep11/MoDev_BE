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
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.global.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "tech_stacks")
class TechStack(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "public_id", unique = true, nullable = false, updatable = false, length = 100)
    val publicId: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = true)
    val description: String? = null,
    @Column(nullable = true)
    var version: String? = null,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "field_id",
        foreignKey = ForeignKey(name = "fk_tech_stack_field"),
    )
    val field: Field,
    @Column(name = "icon_url", nullable = true)
    val iconUrl: String? = null,
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
