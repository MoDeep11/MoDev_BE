package modeep.modev.domain.project.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ProjectFieldId(
    @Column(name = "project_id", length = 50)
    val projectId: String = "",
    @Column(name = "field_id")
    val fieldId: Long = 0L,
) : Serializable
