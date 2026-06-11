package modeep.modev.domain.project.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ProjectDependencyId(
    @Column(name = "project_id", length = 50)
    val projectId: String,
    @Column(name = "dependency_id")
    val dependencyId: Long = 0L,
) : Serializable
