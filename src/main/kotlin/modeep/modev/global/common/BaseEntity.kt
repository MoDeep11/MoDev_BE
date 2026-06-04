package modeep.modev.global.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import java.time.ZoneOffset

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
)
