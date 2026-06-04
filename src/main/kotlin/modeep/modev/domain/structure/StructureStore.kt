package modeep.modev.domain.structure

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class StructureStore {
    private val storage = ConcurrentHashMap<Long, String>()

    fun save(
        projectId: Long,
        structure: String,
    ) {
        storage[projectId] = structure
    }

    fun find(projectId: Long): String? {
        return storage[projectId]
    }

    fun exists(projectId: Long): Boolean {
        return storage.containsKey(projectId)
    }

    fun delete(projectId: Long) {
        storage.remove(projectId)
    }
}
