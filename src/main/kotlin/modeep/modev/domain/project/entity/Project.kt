package modeep.modev.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "projects")
class Project(
    @Id
    @Column(name = "project_id", nullable = false, length = 50)
    val projectId: String,
    @Column(name = "user_id")
    val userId: Long? = null,
    @Column(name = "generate_id", nullable = false, length = 50)
    val generateId: String,
    @Column(name = "project_name", nullable = false, length = 50)
    var projectName: String,
    @Column(length = 500)
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProjectStatus = ProjectStatus.ACTIVE,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = createdAt,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
    @Column(name = "hard_delete_scheduled_at")
    var hardDeleteScheduledAt: Instant? = null,
) {
    fun updateMetadata(
        projectName: String,
        description: String?,
    ) {
        this.projectName = projectName
        this.description = description
        this.updatedAt = Instant.now()
    }

    fun delete(
        deletedAt: Instant,
        hardDeleteScheduledAt: Instant,
    ) {
        this.deletedAt = deletedAt
        this.hardDeleteScheduledAt = hardDeleteScheduledAt
        this.status = ProjectStatus.DELETED
        this.updatedAt = deletedAt
    }
}
