# Tech Stack Catalog Data

로컬/dev DB에 기본으로 들어가야 할 tech stack 목록이다.

Registry 동기화 API는 `public_id`로 `tech_stacks` row를 찾은 뒤 `registry_type`, `registry_identifier`를 사용한다. 따라서 registry API 테스트 대상은 두 registry 컬럼이 필요하다.

먼저 [field.md](field.md)의 field 데이터를 넣은 뒤 실행한다.

## Tech Stacks

| public_id | field_public_id | name | category | version | registry_type | registry_identifier | registry_auto_sync |
| --- | --- | --- | --- | --- | --- | --- | --- |
| stack_spring | field_backend | Spring Boot | FRAMEWORK | 3.5.0 | MAVEN_CENTRAL | org.springframework.boot:spring-boot-starter-web | true |
| stack_nestjs | field_backend | NestJS | FRAMEWORK | 11.0.0 | NPM | @nestjs/core | true |
| stack_fastapi | field_backend | FastAPI | FRAMEWORK | 0.115.0 | PYPI | fastapi | true |
| stack_django | field_backend | Django | FRAMEWORK | 5.2.0 | PYPI | django | true |
| stack_gin | field_backend | Gin | FRAMEWORK | 1.10.0 | GO_PROXY | github.com/gin-gonic/gin | true |
| stack_react | field_frontend | React | FRAMEWORK | 19.0.0 | NPM | react | true |
| stack_nextjs | field_frontend | Next.js | FRAMEWORK | 15.0.0 | NPM | next | true |
| stack_vue | field_frontend | Vue | FRAMEWORK | 3.5.0 | NPM | vue | true |
| stack_postgresql | field_database | PostgreSQL | DATABASE | 16 | DOCKER_HUB | library/postgres | true |
| stack_mysql | field_database | MySQL | DATABASE | 8.4 | DOCKER_HUB | library/mysql | true |
| stack_redis | field_database | Redis | DATABASE | 7.4 | DOCKER_HUB | library/redis | true |
| stack_docker | field_devops | Docker | TOOL | 27.0.0 | GITHUB_RELEASES | docker/cli | true |
| stack_kubernetes | field_devops | Kubernetes | INFRA | 1.30.0 | GITHUB_RELEASES | kubernetes/kubernetes | true |
| stack_ingress_nginx | field_devops | ingress-nginx | INFRA | 4.11.0 | ARTIFACT_HUB | ingress-nginx/ingress-nginx | true |
| stack_langchain | field_ai | LangChain | FRAMEWORK | 0.3.0 | PYPI | langchain | true |
| stack_openai_python | field_ai | OpenAI Python SDK | TOOL | 1.0.0 | PYPI | openai | true |
