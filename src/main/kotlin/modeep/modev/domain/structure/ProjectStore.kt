package modeep.modev.domain.structure

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import org.springframework.stereotype.Component
import java.util.UUID

// todo: 임시로 사용, project 도메인 개발 이후 삭제

@Component
class ProjectStore {
    fun get(id: UUID): Project? {
        return Project(
            id = id,
            name = "test",
            dependencies = emptyList(),
            techStacks = emptyList(),
            fields = emptyList(),
        )
    }
}

data class Project(
    val id: UUID,
    val name: String,
    val structure: String? = null,
    val fields: List<Field>,
    val techStacks: List<TechStack>,
    val dependencies: List<Dependency>,
)
