package modeep.modev.domain.user.entity

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false, length = 320)
    val email: String,
    @Column(nullable = false)
    val passwordHash: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.UNVERIFIED,
    @Column(nullable = true)
    val deletedAt: Instant? = null,
) : BaseEntity() {
    fun verifyEmail() {
        status = UserStatus.ACTIVE
    }

    companion object {
        fun create(
            email: String,
            passwordHash: String,
        ) = User(email = email, passwordHash = passwordHash)
    }
}
