package modeep.modev.domain.project.repository

import jakarta.persistence.LockModeType
import modeep.modev.domain.project.entity.Project
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {
    fun findByIdAndDeletedAtIsNull(projectId: UUID): Project?

    fun findAllByUserIdIsNullAndDeletedAtLessThanEqual(deletedAt: Instant): List<Project>
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :projectId and p.deletedAt is null")
    fun findByIdAndDeletedAtIsNullForUpdate(
        @Param("projectId") projectId: UUID,
    ): Project?

    fun findByDeletedAtIsNull(pageable: Pageable): Page<Project>

    fun findByUserIdAndDeletedAtIsNull(
        userId: Long,
        pageable: Pageable,
    ): Page<Project>

    fun findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull(
        projectName: String,
        pageable: Pageable,
    ): Page<Project>

    fun findByUserIdAndProjectNameContainingIgnoreCaseAndDeletedAtIsNull(
        userId: Long,
        projectName: String,
        pageable: Pageable,
    ): Page<Project>
}
