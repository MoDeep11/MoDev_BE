package modeep.modev.domain.catalog.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class FieldStackMappingId(
    @Column(name = "field_id")
    val fieldId: Long = 0L,
    @Column(name = "tech_stack_id")
    val techStackId: Long = 0L,
) : Serializable
