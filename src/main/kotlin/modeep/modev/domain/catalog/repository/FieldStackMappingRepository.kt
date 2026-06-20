package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.FieldStackMapping
import modeep.modev.domain.catalog.entity.id.FieldStackMappingId
import org.springframework.data.jpa.repository.JpaRepository

interface FieldStackMappingRepository : JpaRepository<FieldStackMapping, FieldStackMappingId>
