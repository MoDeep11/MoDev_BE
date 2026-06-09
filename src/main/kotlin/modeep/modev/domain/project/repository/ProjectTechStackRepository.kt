package modeep.modev.domain.project.repository

import modeep.modev.domain.project.entity.ProjectTechStack
import modeep.modev.domain.project.entity.id.ProjectTechStackId
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectTechStackRepository : JpaRepository<ProjectTechStack, ProjectTechStackId>
