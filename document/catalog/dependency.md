# Dependency Catalog Data

로컬/dev DB에 기본으로 들어가야 할 dependency 목록이다.

Registry 동기화 API는 `public_id`로 `dependencies` row를 찾은 뒤 `registry_type`, `registry_identifier`를 사용한다. 따라서 registry API 테스트 대상은 두 registry 컬럼이 필요하다.

먼저 [field.md](field.md), [tech-stack.md](tech-stack.md)의 데이터를 넣은 뒤 실행한다.

## Dependencies

| public_id | tech_stack_public_id | name | version | is_recommended | registry_type | registry_identifier | document_url |
| --- | --- | --- | --- | --- | --- | --- | --- |
| dep_spring_security | stack_spring | Spring Security | 6.5.0 | true | MAVEN_CENTRAL | org.springframework.boot:spring-boot-starter-security | https://docs.spring.io/spring-security/reference/ |
| dep_spring_data_jpa | stack_spring | Spring Data JPA | 3.5.0 | true | MAVEN_CENTRAL | org.springframework.boot:spring-boot-starter-data-jpa | https://spring.io/projects/spring-data-jpa |
| dep_spring_validation | stack_spring | Spring Validation | 3.5.0 | true | MAVEN_CENTRAL | org.springframework.boot:spring-boot-starter-validation | https://docs.spring.io/spring-framework/reference/core/validation/ |
| dep_nest_config | stack_nestjs | Nest Config | 4.0.0 | true | NPM | @nestjs/config | https://docs.nestjs.com/techniques/configuration |
| dep_nest_typeorm | stack_nestjs | Nest TypeORM | 11.0.0 | false | NPM | @nestjs/typeorm | https://docs.nestjs.com/techniques/database |
| dep_fastapi_uvicorn | stack_fastapi | Uvicorn | 0.34.0 | true | PYPI | uvicorn | https://www.uvicorn.org/ |
| dep_fastapi_sqlalchemy | stack_fastapi | SQLAlchemy | 2.0.0 | false | PYPI | SQLAlchemy | https://www.sqlalchemy.org/ |
| dep_django_rest_framework | stack_django | Django REST framework | 3.15.0 | true | PYPI | djangorestframework | https://www.django-rest-framework.org/ |
| dep_gin_cors | stack_gin | gin-contrib/cors | 1.7.0 | true | GO_PROXY | github.com/gin-contrib/cors | https://github.com/gin-contrib/cors |
| dep_react_router | stack_react | React Router | 7.0.0 | true | NPM | react-router | https://reactrouter.com/ |
| dep_react_zustand | stack_react | Zustand | 5.0.0 | true | NPM | zustand | https://zustand-demo.pmnd.rs/ |
| dep_react_query | stack_react | TanStack Query | 5.0.0 | true | NPM | @tanstack/react-query | https://tanstack.com/query/latest |
| dep_next_auth | stack_nextjs | NextAuth.js | 4.24.0 | true | NPM | next-auth | https://next-auth.js.org/ |
| dep_vue_pinia | stack_vue | Pinia | 3.0.0 | true | NPM | pinia | https://pinia.vuejs.org/ |
| dep_postgresql_pgvector | stack_postgresql | pgvector | 0.8.0 | false | GITHUB_RELEASES | pgvector/pgvector | https://github.com/pgvector/pgvector |
| dep_redis_om_node | stack_redis | Redis OM Node | 0.4.0 | false | NPM | redis-om | https://github.com/redis/redis-om-node |
| dep_kubernetes_helm | stack_kubernetes | Helm | 3.15.0 | true | GITHUB_RELEASES | helm/helm | https://helm.sh/docs/ |
| dep_langchain_openai | stack_langchain | langchain-openai | 0.2.0 | true | PYPI | langchain-openai | https://python.langchain.com/docs/integrations/llms/openai/ |
| dep_openai_tiktoken | stack_openai_python | tiktoken | 0.7.0 | false | PYPI | tiktoken | https://github.com/openai/tiktoken |
