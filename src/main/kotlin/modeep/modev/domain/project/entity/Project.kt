package modeep.modev.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import modeep.modev.global.common.BaseEntity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "projects")
class Project(
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "project_id", nullable = false, length = 50)
    val id: UUID? = null,
    @Column(name = "user_id")
    val userId: Long? = null,
    @Column(name = "project_name", nullable = false, length = 50)
    var projectName: String,
    @Column(length = 500)
    var description: String? = null,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var structure: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProjectStatus = ProjectStatus.ACTIVE,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) : BaseEntity() {
    fun updateMetadata(
        projectName: String,
        description: String?,
    ) {
        require(projectName.isNotBlank()) { "프로젝트 이름은 비어있을 수 없습니다" }
        require(projectName.length <= 50) { "프로젝트 이름은 50자를 초과할 수 없습니다" }
        description?.let {
            require(it.length <= 500) { "설명은 500자를 초과할 수 없습니다" }
        }
        this.projectName = projectName
        this.description = description
    }

    fun delete(deletedAt: Instant) {
        this.deletedAt = deletedAt
    }
}
