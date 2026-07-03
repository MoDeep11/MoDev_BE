package modeep.modev.domain.project.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class ProjectTechStackId(
    @Column(name = "project_id", length = 50)
    val projectId: UUID? = null,
    @Column(name = "tech_stack_id")
    val techStackId: Long = 0L,
) : Serializable
