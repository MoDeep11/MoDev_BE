package modeep.modev.domain.catalog.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import modeep.modev.global.common.BaseEntity

@Entity
@Table(name = "tech_stacks")
class TechStack(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "public_id", unique = true, nullable = false, updatable = false, length = 100)
    val publicId: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = true)
    val description: String? = null,
    @Column(nullable = true)
    val version: String? = null,
) : BaseEntity()
