package modeep.modev.domain.structure

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// todo: 임시로 사용, project 도메인 개발 이후 삭제

@Component
class ProjectStore {
    private val statuses = ConcurrentHashMap<UUID, String>()
    private val structures = ConcurrentHashMap<UUID, String>()

    fun get(id: UUID): Project? {
        return Project(
            id = id,
            name = "test",
            structure = structures[id],
            status = statuses[id],
            dependencies = emptyList(),
            techStacks = emptyList(),
            fields = emptyList(),
        )
    }

    fun updateStatus(
        id: UUID,
        status: String,
    ) {
        statuses[id] = status
    }

    fun updateStructure(
        id: UUID,
        structure: String,
    ) {
        structures[id] = structure
    }
}

data class Project(
    val id: UUID,
    val name: String,
    val structure: String? = null,
    val status: String? = null,
    val fields: List<Field>,
    val techStacks: List<TechStack>,
    val dependencies: List<Dependency>,
)
