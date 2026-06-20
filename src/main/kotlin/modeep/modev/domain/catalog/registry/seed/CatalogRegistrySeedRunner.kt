package modeep.modev.domain.catalog.registry.seed

import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.Field
import modeep.modev.domain.catalog.entity.FieldStackMapping
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.id.FieldStackMappingId
import modeep.modev.domain.catalog.entity.vo.Category
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.FieldStackMappingRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("dev")
@Component
class CatalogRegistrySeedRunner(
    private val fieldRepository: FieldRepository,
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val fieldStackMappingRepository: FieldStackMappingRepository,
) : ApplicationRunner {
    @Transactional
    override fun run(args: ApplicationArguments) {
        seedFields()
        seedTechStacks()
        seedDependencies()
    }

    private fun seedFields() {
        val existingPublicIds = fieldRepository.findByPublicIdIn(fieldSeeds.map { it.publicId }).map { it.publicId }.toSet()
        val fields =
            fieldSeeds
                .filterNot { it.publicId in existingPublicIds }
                .map {
                    Field(
                        publicId = it.publicId,
                        name = it.name,
                        description = it.description,
                        iconUrl = it.iconUrl,
                    )
                }

        fieldRepository.saveAll(fields)
    }

    private fun seedTechStacks() {
        val fieldsByPublicId = fieldRepository.findByPublicIdIn(fieldSeeds.map { it.publicId }).associateBy { it.publicId }
        val existingPublicIds = techStackRepository.findByPublicIdIn(techStackSeeds.map { it.publicId }).map { it.publicId }.toSet()
        val techStacks =
            techStackSeeds
                .filterNot { it.publicId in existingPublicIds }
                .map {
                    TechStack(
                        publicId = it.publicId,
                        name = it.name,
                        description = it.description,
                        version = it.version,
                        category = it.category,
                        field = fieldsByPublicId.getValue(it.fieldPublicId),
                        iconUrl = it.iconUrl,
                        registryType = it.registryType,
                        registryIdentifier = it.registryIdentifier,
                        registryAutoSync = it.registryAutoSync,
                    )
                }

        techStackRepository.saveAll(techStacks)
        seedFieldStackMappings()
    }

    private fun seedFieldStackMappings() {
        val techStacksByPublicId = techStackRepository.findByPublicIdIn(techStackSeeds.map { it.publicId }).associateBy { it.publicId }
        val mappings =
            techStackSeeds.map {
                val techStack = techStacksByPublicId.getValue(it.publicId)

                FieldStackMappingId(
                    fieldId = requireNotNull(techStack.field.id) { "Field id must not be null: ${it.fieldPublicId}" },
                    techStackId = requireNotNull(techStack.id) { "TechStack id must not be null: ${it.publicId}" },
                )
            }
        val existingMappingIds = fieldStackMappingRepository.findAllById(mappings).map { it.id }.toSet()
        val missingMappings =
            mappings
                .filterNot { it in existingMappingIds }
                .map { FieldStackMapping(id = it) }

        fieldStackMappingRepository.saveAll(missingMappings)
    }

    private fun seedDependencies() {
        val techStacksByPublicId = techStackRepository.findByPublicIdIn(techStackSeeds.map { it.publicId }).associateBy { it.publicId }
        val existingPublicIds =
            dependencyRepository.findByPublicIdIn(dependencySeeds.map { it.publicId }).map { it.publicId }.toSet()
        val dependencies =
            dependencySeeds
                .filterNot { it.publicId in existingPublicIds }
                .map {
                    Dependency(
                        publicId = it.publicId,
                        techStack = techStacksByPublicId.getValue(it.techStackPublicId),
                        name = it.name,
                        description = it.description,
                        version = it.version,
                        isRecommended = it.isRecommended,
                        documentUrl = it.documentUrl,
                        registryType = it.registryType,
                        registryIdentifier = it.registryIdentifier,
                        registryAutoSync = it.registryAutoSync,
                    )
                }

        dependencyRepository.saveAll(dependencies)
    }

    private data class FieldSeed(
        val publicId: String,
        val name: String,
        val description: String,
        val iconUrl: String,
    )

    private data class TechStackSeed(
        val publicId: String,
        val fieldPublicId: String,
        val name: String,
        val description: String,
        val version: String,
        val category: Category,
        val iconUrl: String,
        val registryType: RegistryType,
        val registryIdentifier: String,
        val registryAutoSync: Boolean = true,
    )

    private data class DependencySeed(
        val publicId: String,
        val techStackPublicId: String,
        val name: String,
        val description: String,
        val version: String,
        val isRecommended: Boolean,
        val documentUrl: String,
        val registryType: RegistryType,
        val registryIdentifier: String,
        val registryAutoSync: Boolean = true,
    )

    private companion object {
        private val fieldSeeds =
            listOf(
                FieldSeed("field_backend", "Backend", "서버 애플리케이션과 API 개발", "https://cdn.example.com/icons/backend.svg"),
                FieldSeed("field_frontend", "Frontend", "웹 UI와 클라이언트 애플리케이션 개발", "https://cdn.example.com/icons/frontend.svg"),
                FieldSeed("field_database", "Database", "데이터 저장소와 캐시 구성", "https://cdn.example.com/icons/database.svg"),
                FieldSeed("field_devops", "DevOps", "배포, 운영, 인프라 자동화", "https://cdn.example.com/icons/devops.svg"),
                FieldSeed("field_ai", "AI", "AI 애플리케이션과 모델 연동", "https://cdn.example.com/icons/ai.svg"),
            )

        private val techStackSeeds =
            listOf(
                TechStackSeed(
                    "stack_spring",
                    "field_backend",
                    "Spring Boot",
                    "Kotlin/Java 기반 백엔드 애플리케이션 프레임워크",
                    "3.5.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/spring.svg",
                    RegistryType.MAVEN_CENTRAL,
                    "org.springframework.boot:spring-boot-starter-web",
                ),
                TechStackSeed(
                    "stack_nestjs",
                    "field_backend",
                    "NestJS",
                    "Node.js 기반 서버 애플리케이션 프레임워크",
                    "11.0.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/nestjs.svg",
                    RegistryType.NPM,
                    "@nestjs/core",
                ),
                TechStackSeed(
                    "stack_fastapi",
                    "field_backend",
                    "FastAPI",
                    "Python 기반 고성능 API 프레임워크",
                    "0.115.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/fastapi.svg",
                    RegistryType.PYPI,
                    "fastapi",
                ),
                TechStackSeed(
                    "stack_django",
                    "field_backend",
                    "Django",
                    "Python 기반 풀스택 웹 프레임워크",
                    "5.2.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/django.svg",
                    RegistryType.PYPI,
                    "django",
                ),
                TechStackSeed(
                    "stack_gin",
                    "field_backend",
                    "Gin",
                    "Go 기반 HTTP 웹 프레임워크",
                    "1.10.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/gin.svg",
                    RegistryType.GO_PROXY,
                    "github.com/gin-gonic/gin",
                ),
                TechStackSeed(
                    "stack_react",
                    "field_frontend",
                    "React",
                    "컴포넌트 기반 웹 UI 라이브러리",
                    "19.0.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/react.svg",
                    RegistryType.NPM,
                    "react",
                ),
                TechStackSeed(
                    "stack_nextjs",
                    "field_frontend",
                    "Next.js",
                    "React 기반 풀스택 웹 프레임워크",
                    "15.0.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/nextjs.svg",
                    RegistryType.NPM,
                    "next",
                ),
                TechStackSeed(
                    "stack_vue",
                    "field_frontend",
                    "Vue",
                    "프로그레시브 웹 UI 프레임워크",
                    "3.5.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/vue.svg",
                    RegistryType.NPM,
                    "vue",
                ),
                TechStackSeed(
                    "stack_postgresql",
                    "field_database",
                    "PostgreSQL",
                    "오픈소스 관계형 데이터베이스",
                    "16",
                    Category.DATABASE,
                    "https://cdn.example.com/icons/postgresql.svg",
                    RegistryType.DOCKER_HUB,
                    "library/postgres",
                ),
                TechStackSeed(
                    "stack_mysql",
                    "field_database",
                    "MySQL",
                    "관계형 데이터베이스",
                    "8.4",
                    Category.DATABASE,
                    "https://cdn.example.com/icons/mysql.svg",
                    RegistryType.DOCKER_HUB,
                    "library/mysql",
                ),
                TechStackSeed(
                    "stack_redis",
                    "field_database",
                    "Redis",
                    "인메모리 데이터 저장소와 캐시",
                    "7.4",
                    Category.DATABASE,
                    "https://cdn.example.com/icons/redis.svg",
                    RegistryType.DOCKER_HUB,
                    "library/redis",
                ),
                TechStackSeed(
                    "stack_docker",
                    "field_devops",
                    "Docker",
                    "컨테이너 이미지 빌드와 실행 도구",
                    "27.0.0",
                    Category.TOOL,
                    "https://cdn.example.com/icons/docker.svg",
                    RegistryType.GITHUB_RELEASES,
                    "docker/cli",
                ),
                TechStackSeed(
                    "stack_kubernetes",
                    "field_devops",
                    "Kubernetes",
                    "컨테이너 오케스트레이션 플랫폼",
                    "1.30.0",
                    Category.INFRA,
                    "https://cdn.example.com/icons/kubernetes.svg",
                    RegistryType.GITHUB_RELEASES,
                    "kubernetes/kubernetes",
                ),
                TechStackSeed(
                    "stack_ingress_nginx",
                    "field_devops",
                    "ingress-nginx",
                    "Kubernetes NGINX Ingress Controller",
                    "4.11.0",
                    Category.INFRA,
                    "https://cdn.example.com/icons/nginx.svg",
                    RegistryType.ARTIFACT_HUB,
                    "ingress-nginx/ingress-nginx",
                ),
                TechStackSeed(
                    "stack_langchain",
                    "field_ai",
                    "LangChain",
                    "LLM 애플리케이션 개발 프레임워크",
                    "0.3.0",
                    Category.FRAMEWORK,
                    "https://cdn.example.com/icons/langchain.svg",
                    RegistryType.PYPI,
                    "langchain",
                ),
                TechStackSeed(
                    "stack_openai_python",
                    "field_ai",
                    "OpenAI Python SDK",
                    "OpenAI API Python 클라이언트",
                    "1.0.0",
                    Category.TOOL,
                    "https://cdn.example.com/icons/openai.svg",
                    RegistryType.PYPI,
                    "openai",
                ),
            )

        private val dependencySeeds =
            listOf(
                DependencySeed(
                    "dep_spring_security",
                    "stack_spring",
                    "Spring Security",
                    "Spring 기반 인증과 인가 지원",
                    "6.5.0",
                    true,
                    "https://docs.spring.io/spring-security/reference/",
                    RegistryType.MAVEN_CENTRAL,
                    "org.springframework.boot:spring-boot-starter-security",
                ),
                DependencySeed(
                    "dep_spring_data_jpa",
                    "stack_spring",
                    "Spring Data JPA",
                    "JPA 기반 데이터 접근 추상화",
                    "3.5.0",
                    true,
                    "https://spring.io/projects/spring-data-jpa",
                    RegistryType.MAVEN_CENTRAL,
                    "org.springframework.boot:spring-boot-starter-data-jpa",
                ),
                DependencySeed(
                    "dep_spring_validation",
                    "stack_spring",
                    "Spring Validation",
                    "요청 데이터 검증 지원",
                    "3.5.0",
                    true,
                    "https://docs.spring.io/spring-framework/reference/core/validation/",
                    RegistryType.MAVEN_CENTRAL,
                    "org.springframework.boot:spring-boot-starter-validation",
                ),
                DependencySeed(
                    "dep_nest_config",
                    "stack_nestjs",
                    "Nest Config",
                    "NestJS 설정 모듈",
                    "4.0.0",
                    true,
                    "https://docs.nestjs.com/techniques/configuration",
                    RegistryType.NPM,
                    "@nestjs/config",
                ),
                DependencySeed(
                    "dep_nest_typeorm",
                    "stack_nestjs",
                    "Nest TypeORM",
                    "NestJS TypeORM 연동 모듈",
                    "11.0.0",
                    false,
                    "https://docs.nestjs.com/techniques/database",
                    RegistryType.NPM,
                    "@nestjs/typeorm",
                ),
                DependencySeed(
                    "dep_fastapi_uvicorn",
                    "stack_fastapi",
                    "Uvicorn",
                    "ASGI 서버",
                    "0.34.0",
                    true,
                    "https://www.uvicorn.org/",
                    RegistryType.PYPI,
                    "uvicorn",
                ),
                DependencySeed(
                    "dep_fastapi_sqlalchemy",
                    "stack_fastapi",
                    "SQLAlchemy",
                    "Python SQL toolkit과 ORM",
                    "2.0.0",
                    false,
                    "https://www.sqlalchemy.org/",
                    RegistryType.PYPI,
                    "SQLAlchemy",
                ),
                DependencySeed(
                    "dep_django_rest_framework",
                    "stack_django",
                    "Django REST framework",
                    "Django API 개발 도구",
                    "3.15.0",
                    true,
                    "https://www.django-rest-framework.org/",
                    RegistryType.PYPI,
                    "djangorestframework",
                ),
                DependencySeed(
                    "dep_gin_cors",
                    "stack_gin",
                    "gin-contrib/cors",
                    "Gin CORS middleware",
                    "1.7.0",
                    true,
                    "https://github.com/gin-contrib/cors",
                    RegistryType.GO_PROXY,
                    "github.com/gin-contrib/cors",
                ),
                DependencySeed(
                    "dep_react_router",
                    "stack_react",
                    "React Router",
                    "React 라우팅 라이브러리",
                    "7.0.0",
                    true,
                    "https://reactrouter.com/",
                    RegistryType.NPM,
                    "react-router",
                ),
                DependencySeed(
                    "dep_react_zustand",
                    "stack_react",
                    "Zustand",
                    "React 상태 관리 라이브러리",
                    "5.0.0",
                    true,
                    "https://zustand-demo.pmnd.rs/",
                    RegistryType.NPM,
                    "zustand",
                ),
                DependencySeed(
                    "dep_react_query",
                    "stack_react",
                    "TanStack Query",
                    "서버 상태 관리 라이브러리",
                    "5.0.0",
                    true,
                    "https://tanstack.com/query/latest",
                    RegistryType.NPM,
                    "@tanstack/react-query",
                ),
                DependencySeed(
                    "dep_next_auth",
                    "stack_nextjs",
                    "NextAuth.js",
                    "Next.js 인증 라이브러리",
                    "4.24.0",
                    true,
                    "https://next-auth.js.org/",
                    RegistryType.NPM,
                    "next-auth",
                ),
                DependencySeed(
                    "dep_vue_pinia",
                    "stack_vue",
                    "Pinia",
                    "Vue 상태 관리 라이브러리",
                    "3.0.0",
                    true,
                    "https://pinia.vuejs.org/",
                    RegistryType.NPM,
                    "pinia",
                ),
                DependencySeed(
                    "dep_postgresql_pgvector",
                    "stack_postgresql",
                    "pgvector",
                    "PostgreSQL vector extension",
                    "0.8.0",
                    false,
                    "https://github.com/pgvector/pgvector",
                    RegistryType.GITHUB_RELEASES,
                    "pgvector/pgvector",
                ),
                DependencySeed(
                    "dep_redis_om_node",
                    "stack_redis",
                    "Redis OM Node",
                    "Node.js용 Redis OM",
                    "0.4.0",
                    false,
                    "https://github.com/redis/redis-om-node",
                    RegistryType.NPM,
                    "redis-om",
                ),
                DependencySeed(
                    "dep_kubernetes_helm",
                    "stack_kubernetes",
                    "Helm",
                    "Kubernetes package manager",
                    "3.15.0",
                    true,
                    "https://helm.sh/docs/",
                    RegistryType.GITHUB_RELEASES,
                    "helm/helm",
                ),
                DependencySeed(
                    "dep_langchain_openai",
                    "stack_langchain",
                    "langchain-openai",
                    "LangChain OpenAI integration",
                    "0.2.0",
                    true,
                    "https://python.langchain.com/docs/integrations/llms/openai/",
                    RegistryType.PYPI,
                    "langchain-openai",
                ),
                DependencySeed(
                    "dep_openai_tiktoken",
                    "stack_openai_python",
                    "tiktoken",
                    "OpenAI tokenizer library",
                    "0.7.0",
                    false,
                    "https://github.com/openai/tiktoken",
                    RegistryType.PYPI,
                    "tiktoken",
                ),
            )
    }
}
