package modeep.modev.domain.project.repository

import modeep.modev.domain.project.entity.ProjectDependency
import modeep.modev.domain.project.entity.id.ProjectDependencyId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectDependencyRepository : JpaRepository<ProjectDependency, ProjectDependencyId> {
    fun deleteAllByIdProjectId(projectId: UUID)
}
