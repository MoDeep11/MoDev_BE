package modeep.modev.domain.project.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.project.entity.id.ProjectDependencyId

@Entity
@Table(name = "project_dependencies")
class ProjectDependency(
    @EmbeddedId
    val id: ProjectDependencyId,
)
