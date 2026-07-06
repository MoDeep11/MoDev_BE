package modeep.modev.domain.project.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.project.entity.id.ProjectDependencyId

@Entity
@Table(name = "project_dependencies")
class ProjectDependency(
    @EmbeddedId
    val id: ProjectDependencyId,
    @Column(nullable = true)
    var version: String? = null,
)
