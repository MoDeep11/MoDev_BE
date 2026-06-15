package modeep.modev.domain.structure.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.global.common.BaseEntity
import modeep.modev.global.util.LanguageDetector
import java.util.UUID

@Entity
@Table(
    name = "structure_files",
    // 유니크 제약, project_id, path 중복 불가
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_structure_files_project_path",
            columnNames = ["project_id", "path"],
        ),
    ],
)
class StructureFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "project_id", nullable = false, updatable = false)
    val projectId: UUID,
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var type: StructureFileType,
    @Column(nullable = false, length = 1000)
    val path: String,
    @Column(nullable = false)
    var depth: Int,
    @Lob
    @Column(nullable = true, columnDefinition = "TEXT")
    var content: String? = null,
    @Column(nullable = false, length = 100)
    var language: String = LanguageDetector.detect(path),
) : BaseEntity() {
    fun update(
        type: StructureFileType,
        depth: Int,
        content: String?,
        language: String,
    ) {
        this.type = type
        this.depth = depth
        this.content = content
        this.language = language
    }
}
