package modeep.modev.domain.auth.repository

import modeep.modev.domain.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmailIgnoreCase(email: String): Boolean

    fun findByEmailIgnoreCase(email: String): User?
}
