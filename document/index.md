## 1. 개요

쿼리 패턴과 NFR 요구사항을 기반으로 인덱스를 정의한다.

- 복합 PK를 사용하는 중간 테이블은 PK 인덱스가 자동 생성되므로 `project_id` 단독 인덱스만 추가한다.
- 검색(NFR-PERF-04)은 Full-Text Search 인덱스를 사용한다.

---

## 2. 테이블별 인덱스

### User

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `uk_user_email` | `email` | UNIQUE | 로그인 시 email로 조회 |
| `idx_user_status` | `status` | 일반 | 상태별 필터링 |

---

### Project

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_project_user_id_deleted_at` | `(user_id, deleted_at)` | 복합 | 사용자별 프로젝트 목록 조회 + 삭제 여부 필터링 (NFR-PERF-09) |

---

### TechStack

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `fts_tech_stack_name` | `name` | Full-Text Search | 스택명 검색 (NFR-PERF-04) |

---

### Dependency

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_dependency_tech_stack_id` | `tech_stack_id` | 일반 | 스택별 의존성 목록 조회 |
| `fts_dependency_name` | `name` | Full-Text Search | 의존성명 검색 (NFR-PERF-04) |

---

### FieldStackMapping

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_field_stack_mapping_field_id` | `field_id` | 일반 | 분야별 스택 목록 조회 |
| `idx_field_stack_mapping_tech_stack_id` | `tech_stack_id` | 일반 | 스택별 분야 역방향 조회 |

---

### ProjectField

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_project_field_project_id` | `project_id` | 일반 | 프로젝트별 분야 조회 |

---

### ProjectTechStack

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_project_tech_stack_project_id` | `project_id` | 일반 | 프로젝트별 스택 조회 |

---

### ProjectDependency

| 인덱스명 | 컬럼 | 종류 | 이유 |
| --- | --- | --- | --- |
| `idx_project_dependency_project_id` | `project_id` | 일반 | 프로젝트별 의존성 조회 |

---

## 3. DDL 예시 (PostgreSQL)

```sql
-- User
CREATE UNIQUE INDEX uk_user_email ON users (email);
CREATE INDEX idx_user_status ON users (status);

-- Project
CREATE INDEX idx_project_user_id_deleted_at ON projects (user_id, deleted_at);

-- TechStack
CREATE INDEX fts_tech_stack_name ON tech_stacks USING gin (to_tsvector('simple', name));

-- Dependency
CREATE INDEX idx_dependency_tech_stack_id ON dependencies (tech_stack_id);
CREATE INDEX fts_dependency_name ON dependencies USING gin (to_tsvector('simple', name));

-- FieldStackMapping
CREATE INDEX idx_field_stack_mapping_field_id ON field_stack_mappings (field_id);
CREATE INDEX idx_field_stack_mapping_tech_stack_id ON field_stack_mappings (tech_stack_id);

-- ProjectField
CREATE INDEX idx_project_field_project_id ON project_fields (project_id);

-- ProjectTechStack
CREATE INDEX idx_project_tech_stack_project_id ON project_tech_stacks (project_id);

-- ProjectDependency
CREATE INDEX idx_project_dependency_project_id ON project_dependencies (project_id);
```

---

## 4. FTS 쿼리 예시

```sql
-- TechStack 검색
SELECT * FROM tech_stacks
WHERE to_tsvector('simple', name) @@ plainto_tsquery('simple', 'spring boot');

-- Dependency 검색
SELECT * FROM dependencies
WHERE to_tsvector('simple', name) @@ plainto_tsquery('simple', 'security');
```

> 현재 `simple` config를 사용한다. 향후 한글 검색이 필요해지면 `pg_bigm` 확장으로 전환한다.
>