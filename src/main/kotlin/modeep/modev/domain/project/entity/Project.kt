package modeep.modev.domain.project.entity

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "projects")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id")
    val userId: Long? = null,
    @Column(name = "project_name", nullable = false)
    var name: String,
    @Column(length = 500)
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProjectStatus = ProjectStatus.PENDING,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var structure: JsonNode? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = createdAt,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) {
    fun updateMetadata(
        projectName: String,
        description: String?,
    ) {
        this.name = projectName
        this.description = description
        this.updatedAt = Instant.now()
    }

    fun delete(deletedAt: Instant) {
        this.deletedAt = deletedAt
        this.updatedAt = deletedAt
    }
}
