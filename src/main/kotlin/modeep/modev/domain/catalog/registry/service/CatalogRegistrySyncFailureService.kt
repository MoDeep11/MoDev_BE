package modeep.modev.domain.catalog.registry.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CatalogRegistrySyncFailureService(
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
) {
    // tech stack 동기화 실패 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordTechStackFailure(
        publicId: String,
        throwable: Throwable,
    ) {
        techStackRepository.findByPublicId(publicId)?.also {
            it.recordRegistrySyncFailure(throwable.message ?: throwable::class.simpleName ?: "Unknown error")
            techStackRepository.save(it)
        }
    }

    // dependency 동기화 실패 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordDependencyFailure(
        publicId: String,
        throwable: Throwable,
    ) {
        dependencyRepository.findByPublicId(publicId)?.also {
            it.recordRegistrySyncFailure(throwable.message ?: throwable::class.simpleName ?: "Unknown error")
            dependencyRepository.save(it)
        }
    }
}
