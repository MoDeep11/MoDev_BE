package modeep.modev.domain.project.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.project.entity.id.ProjectTechStackId

@Entity
@Table(name = "project_tech_stacks")
class ProjectTechStack(
    @EmbeddedId
    val id: ProjectTechStackId,
)
