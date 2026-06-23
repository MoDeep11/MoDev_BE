package modeep.modev.domain.user.repository

import modeep.modev.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmailIgnoreCase(email: String): Boolean

    fun findByEmailIgnoreCase(email: String): User?
}
