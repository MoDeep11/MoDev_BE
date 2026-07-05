INSERT INTO fields (public_id, name, description, icon_url)
VALUES
    ('field_backend', 'Backend', '서버 애플리케이션과 API 개발', 'https://cdn.example.com/icons/backend.svg'),
    ('field_frontend', 'Frontend', '웹 UI와 클라이언트 애플리케이션 개발', 'https://cdn.example.com/icons/frontend.svg'),
    ('field_database', 'Database', '데이터 저장소와 캐시 구성', 'https://cdn.example.com/icons/database.svg'),
    ('field_devops', 'DevOps', '배포, 운영, 인프라 자동화', 'https://cdn.example.com/icons/devops.svg'),
    ('field_ai', 'AI', 'AI 애플리케이션과 모델 연동', 'https://cdn.example.com/icons/ai.svg')
ON CONFLICT (public_id) DO NOTHING;

INSERT INTO tech_stacks (
    public_id,
    field_id,
    name,
    description,
    version,
    category,
    icon_url,
    registry_type,
    registry_identifier,
    registry_auto_sync
)
SELECT seed.public_id,
       fields.id,
       seed.name,
       seed.description,
       seed.version,
       seed.category,
       seed.icon_url,
       seed.registry_type,
       seed.registry_identifier,
       seed.registry_auto_sync
FROM (
    VALUES
        ('stack_spring', 'field_backend', 'Spring Boot', 'Kotlin/Java 기반 백엔드 애플리케이션 프레임워크', '3.5.0', 'FRAMEWORK', 'https://cdn.example.com/icons/spring.svg', 'MAVEN_CENTRAL', 'org.springframework.boot:spring-boot-starter-web', TRUE),
        ('stack_nestjs', 'field_backend', 'NestJS', 'Node.js 기반 서버 애플리케이션 프레임워크', '11.0.0', 'FRAMEWORK', 'https://cdn.example.com/icons/nestjs.svg', 'NPM', '@nestjs/core', TRUE),
        ('stack_fastapi', 'field_backend', 'FastAPI', 'Python 기반 고성능 API 프레임워크', '0.115.0', 'FRAMEWORK', 'https://cdn.example.com/icons/fastapi.svg', 'PYPI', 'fastapi', TRUE),
        ('stack_django', 'field_backend', 'Django', 'Python 기반 풀스택 웹 프레임워크', '5.2.0', 'FRAMEWORK', 'https://cdn.example.com/icons/django.svg', 'PYPI', 'django', TRUE),
        ('stack_gin', 'field_backend', 'Gin', 'Go 기반 HTTP 웹 프레임워크', '1.10.0', 'FRAMEWORK', 'https://cdn.example.com/icons/gin.svg', 'GO_PROXY', 'github.com/gin-gonic/gin', TRUE),
        ('stack_react', 'field_frontend', 'React', '컴포넌트 기반 웹 UI 라이브러리', '19.0.0', 'FRAMEWORK', 'https://cdn.example.com/icons/react.svg', 'NPM', 'react', TRUE),
        ('stack_nextjs', 'field_frontend', 'Next.js', 'React 기반 풀스택 웹 프레임워크', '15.0.0', 'FRAMEWORK', 'https://cdn.example.com/icons/nextjs.svg', 'NPM', 'next', TRUE),
        ('stack_vue', 'field_frontend', 'Vue', '프로그레시브 웹 UI 프레임워크', '3.5.0', 'FRAMEWORK', 'https://cdn.example.com/icons/vue.svg', 'NPM', 'vue', TRUE),
        ('stack_postgresql', 'field_database', 'PostgreSQL', '오픈소스 관계형 데이터베이스', '16', 'DATABASE', 'https://cdn.example.com/icons/postgresql.svg', 'DOCKER_HUB', 'library/postgres', TRUE),
        ('stack_mysql', 'field_database', 'MySQL', '관계형 데이터베이스', '8.4', 'DATABASE', 'https://cdn.example.com/icons/mysql.svg', 'DOCKER_HUB', 'library/mysql', TRUE),
        ('stack_redis', 'field_database', 'Redis', '인메모리 데이터 저장소와 캐시', '7.4', 'DATABASE', 'https://cdn.example.com/icons/redis.svg', 'DOCKER_HUB', 'library/redis', TRUE),
        ('stack_docker', 'field_devops', 'Docker', '컨테이너 이미지 빌드와 실행 도구', '27.0.0', 'TOOL', 'https://cdn.example.com/icons/docker.svg', 'GITHUB_RELEASES', 'docker/cli', TRUE),
        ('stack_kubernetes', 'field_devops', 'Kubernetes', '컨테이너 오케스트레이션 플랫폼', '1.30.0', 'INFRA', 'https://cdn.example.com/icons/kubernetes.svg', 'GITHUB_RELEASES', 'kubernetes/kubernetes', TRUE),
        ('stack_ingress_nginx', 'field_devops', 'ingress-nginx', 'Kubernetes NGINX Ingress Controller', '4.11.0', 'INFRA', 'https://cdn.example.com/icons/nginx.svg', 'ARTIFACT_HUB', 'ingress-nginx/ingress-nginx', TRUE),
        ('stack_langchain', 'field_ai', 'LangChain', 'LLM 애플리케이션 개발 프레임워크', '0.3.0', 'FRAMEWORK', 'https://cdn.example.com/icons/langchain.svg', 'PYPI', 'langchain', TRUE),
        ('stack_openai_python', 'field_ai', 'OpenAI Python SDK', 'OpenAI API Python 클라이언트', '1.0.0', 'TOOL', 'https://cdn.example.com/icons/openai.svg', 'PYPI', 'openai', TRUE)
) AS seed(public_id, field_public_id, name, description, version, category, icon_url, registry_type, registry_identifier, registry_auto_sync)
JOIN fields ON fields.public_id = seed.field_public_id
ON CONFLICT (public_id) DO NOTHING;

INSERT INTO field_stack_mappings (field_id, tech_stack_id)
SELECT fields.id, tech_stacks.id
FROM tech_stacks
JOIN fields ON fields.id = tech_stacks.field_id
WHERE tech_stacks.public_id IN (
    'stack_spring',
    'stack_nestjs',
    'stack_fastapi',
    'stack_django',
    'stack_gin',
    'stack_react',
    'stack_nextjs',
    'stack_vue',
    'stack_postgresql',
    'stack_mysql',
    'stack_redis',
    'stack_docker',
    'stack_kubernetes',
    'stack_ingress_nginx',
    'stack_langchain',
    'stack_openai_python'
)
ON CONFLICT (field_id, tech_stack_id) DO NOTHING;

INSERT INTO dependencies (
    public_id,
    tech_stack_id,
    name,
    description,
    version,
    is_recommended,
    document_url,
    registry_type,
    registry_identifier,
    registry_auto_sync
)
SELECT seed.public_id,
       tech_stacks.id,
       seed.name,
       seed.description,
       seed.version,
       seed.is_recommended,
       seed.document_url,
       seed.registry_type,
       seed.registry_identifier,
       seed.registry_auto_sync
FROM (
    VALUES
        ('dep_spring_security', 'stack_spring', 'Spring Security', 'Spring 기반 인증과 인가 지원', '6.5.0', TRUE, 'https://docs.spring.io/spring-security/reference/', 'MAVEN_CENTRAL', 'org.springframework.boot:spring-boot-starter-security', TRUE),
        ('dep_spring_data_jpa', 'stack_spring', 'Spring Data JPA', 'JPA 기반 데이터 접근 추상화', '3.5.0', TRUE, 'https://spring.io/projects/spring-data-jpa', 'MAVEN_CENTRAL', 'org.springframework.boot:spring-boot-starter-data-jpa', TRUE),
        ('dep_spring_validation', 'stack_spring', 'Spring Validation', '요청 데이터 검증 지원', '3.5.0', TRUE, 'https://docs.spring.io/spring-framework/reference/core/validation/', 'MAVEN_CENTRAL', 'org.springframework.boot:spring-boot-starter-validation', TRUE),
        ('dep_nest_config', 'stack_nestjs', 'Nest Config', 'NestJS 설정 모듈', '4.0.0', TRUE, 'https://docs.nestjs.com/techniques/configuration', 'NPM', '@nestjs/config', TRUE),
        ('dep_nest_typeorm', 'stack_nestjs', 'Nest TypeORM', 'NestJS TypeORM 연동 모듈', '11.0.0', FALSE, 'https://docs.nestjs.com/techniques/database', 'NPM', '@nestjs/typeorm', TRUE),
        ('dep_fastapi_uvicorn', 'stack_fastapi', 'Uvicorn', 'ASGI 서버', '0.34.0', TRUE, 'https://www.uvicorn.org/', 'PYPI', 'uvicorn', TRUE),
        ('dep_fastapi_sqlalchemy', 'stack_fastapi', 'SQLAlchemy', 'Python SQL toolkit과 ORM', '2.0.0', FALSE, 'https://www.sqlalchemy.org/', 'PYPI', 'SQLAlchemy', TRUE),
        ('dep_django_rest_framework', 'stack_django', 'Django REST framework', 'Django API 개발 도구', '3.15.0', TRUE, 'https://www.django-rest-framework.org/', 'PYPI', 'djangorestframework', TRUE),
        ('dep_gin_cors', 'stack_gin', 'gin-contrib/cors', 'Gin CORS middleware', '1.7.0', TRUE, 'https://github.com/gin-contrib/cors', 'GO_PROXY', 'github.com/gin-contrib/cors', TRUE),
        ('dep_react_router', 'stack_react', 'React Router', 'React 라우팅 라이브러리', '7.0.0', TRUE, 'https://reactrouter.com/', 'NPM', 'react-router', TRUE),
        ('dep_react_zustand', 'stack_react', 'Zustand', 'React 상태 관리 라이브러리', '5.0.0', TRUE, 'https://zustand-demo.pmnd.rs/', 'NPM', 'zustand', TRUE),
        ('dep_react_query', 'stack_react', 'TanStack Query', '서버 상태 관리 라이브러리', '5.0.0', TRUE, 'https://tanstack.com/query/latest', 'NPM', '@tanstack/react-query', TRUE),
        ('dep_next_auth', 'stack_nextjs', 'NextAuth.js', 'Next.js 인증 라이브러리', '4.24.0', TRUE, 'https://next-auth.js.org/', 'NPM', 'next-auth', TRUE),
        ('dep_vue_pinia', 'stack_vue', 'Pinia', 'Vue 상태 관리 라이브러리', '3.0.0', TRUE, 'https://pinia.vuejs.org/', 'NPM', 'pinia', TRUE),
        ('dep_postgresql_pgvector', 'stack_postgresql', 'pgvector', 'PostgreSQL vector extension', '0.8.0', FALSE, 'https://github.com/pgvector/pgvector', 'GITHUB_RELEASES', 'pgvector/pgvector', TRUE),
        ('dep_redis_om_node', 'stack_redis', 'Redis OM Node', 'Node.js용 Redis OM', '0.4.0', FALSE, 'https://github.com/redis/redis-om-node', 'NPM', 'redis-om', TRUE),
        ('dep_kubernetes_helm', 'stack_kubernetes', 'Helm', 'Kubernetes package manager', '3.15.0', TRUE, 'https://helm.sh/docs/', 'GITHUB_RELEASES', 'helm/helm', TRUE),
        ('dep_langchain_openai', 'stack_langchain', 'langchain-openai', 'LangChain OpenAI integration', '0.2.0', TRUE, 'https://python.langchain.com/docs/integrations/llms/openai/', 'PYPI', 'langchain-openai', TRUE),
        ('dep_openai_tiktoken', 'stack_openai_python', 'tiktoken', 'OpenAI tokenizer library', '0.7.0', FALSE, 'https://github.com/openai/tiktoken', 'PYPI', 'tiktoken', TRUE)
) AS seed(public_id, tech_stack_public_id, name, description, version, is_recommended, document_url, registry_type, registry_identifier, registry_auto_sync)
JOIN tech_stacks ON tech_stacks.public_id = seed.tech_stack_public_id
ON CONFLICT (public_id) DO NOTHING;
