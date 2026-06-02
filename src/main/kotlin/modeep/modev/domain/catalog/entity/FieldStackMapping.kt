package modeep.modev.domain.catalog.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import modeep.modev.domain.catalog.entity.id.FieldStackMappingId
import modeep.modev.global.common.BaseEntity

@Entity
@Table(name = "field_stack_mappings")
class FieldStackMapping(
    @EmbeddedId
    val id: FieldStackMappingId,
) : BaseEntity()
