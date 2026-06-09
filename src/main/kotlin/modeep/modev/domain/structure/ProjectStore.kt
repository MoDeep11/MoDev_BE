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
    private val files = ConcurrentHashMap<UUID, MutableMap<String, StructureFile>>()

    fun get(id: UUID): Project? {
        return Project(
            id = id,
            name = "test",
            dependencies = emptyList(),
            techStacks = emptyList(),
            fields = emptyList(),
        )
    }

    fun updateStatus(
        id: UUID,
        status: String,
    ) = Unit

    fun updateStructure(
        id: UUID,
        structure: String,
    ) = Unit

    fun saveStructureFile(
        id: UUID,
        file: StructureFile,
    ) {
        files.computeIfAbsent(id) { ConcurrentHashMap() }[file.path] = file
    }
}

data class StructureFile(
    val type: String,
    val path: String,
    val depth: Int,
    val content: String?,
)

data class Project(
    val id: UUID,
    val name: String,
    val structure: String? = null,
    val status: String? = null,
    val fields: List<Field>,
    val techStacks: List<TechStack>,
    val dependencies: List<Dependency>,
)
