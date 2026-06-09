package modeep.modev.domain.project.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.project.entity.id.ProjectFieldId

@Entity
@Table(name = "project_fields")
class ProjectField(
    @EmbeddedId
    val id: ProjectFieldId,
)
