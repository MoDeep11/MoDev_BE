package modeep.modev.domain.project.repository

import modeep.modev.domain.project.entity.Project
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {
    fun findByIdAndDeletedAtIsNull(projectId: UUID): Project?

    fun findByDeletedAtIsNull(pageable: Pageable): Page<Project>

    fun findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull(
        projectName: String,
        pageable: Pageable,
    ): Page<Project>
}
