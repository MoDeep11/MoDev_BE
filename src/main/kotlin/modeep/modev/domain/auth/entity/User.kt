package modeep.modev.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import modeep.modev.global.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "public_id", unique = true, nullable = false, updatable = false, length = 100)
    val publicId: String,
    @Column(unique = true, nullable = false, length = 320)
    val email: String,
    @Column(nullable = false)
    val passwordHash: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE,
    @Column(nullable = true)
    val deleted_at: Instant? = null,
) : BaseEntity()
