package modeep.modev.domain.structure.repository

import modeep.modev.domain.structure.entity.StructureFile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StructureFileRepository : JpaRepository<StructureFile, Long> {
    fun findByProjectIdAndPath(
        projectId: UUID,
        path: String,
    ): StructureFile?

    fun findAllByProjectIdOrderByPathAsc(projectId: UUID): List<StructureFile>

    fun deleteAllByProjectId(projectId: UUID)
}
