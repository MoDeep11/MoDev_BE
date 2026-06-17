package modeep.modev.domain.structure.worker.event

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.TechStack
import java.util.UUID

data class GenerateStructureEvent(
    val projectId: UUID,
    val projectName: String,
    val fields: List<FieldInfos>,
    val techStacks: List<StackInfos>,
    val dependencies: List<DependencyInfos> = emptyList(),
)

data class FieldInfos(
    val name: String,
) {
    companion object {
        fun from(field: Field) = FieldInfos(field.name)
    }
}

data class StackInfos(
    val name: String,
    val version: String?,
) {
    companion object {
        fun from(stack: TechStack) =
            StackInfos(
                name = stack.name,
                version = stack.version,
            )
    }
}

data class DependencyInfos(
    val name: String,
    val version: String?,
    val techStackName: String,
) {
    companion object {
        fun from(
            dependency: Dependency,
            techStack: TechStack,
        ) = DependencyInfos(
            name = dependency.name,
            version = dependency.version,
            techStackName = techStack.name,
        )
    }
}
