package modeep.modev.domain.project.repository

import modeep.modev.domain.project.entity.ProjectField
import modeep.modev.domain.project.entity.id.ProjectFieldId
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectFieldRepository : JpaRepository<ProjectField, ProjectFieldId>
