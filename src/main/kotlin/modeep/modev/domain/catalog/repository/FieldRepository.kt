package modeep.modev.domain.catalog.repository

import modeep.modev.domain.catalog.entity.Field
import org.springframework.data.jpa.repository.JpaRepository

interface FieldRepository : JpaRepository<Field, Long> {
    fun findAllByOrderByIdAsc(): List<Field>
}
