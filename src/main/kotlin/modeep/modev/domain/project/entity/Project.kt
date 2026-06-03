package modeep.modev.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "projects")
class Project(
    @Id
    @Column(name = "project_id", nullable = false, length = 50)
    val projectId: String,
    @Column(name = "generate_id", nullable = false, length = 50)
    val generateId: String,
    @Column(name = "project_name", nullable = false, length = 50)
    val projectName: String,
    @Column(name = "description", length = 500)
    val description: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
