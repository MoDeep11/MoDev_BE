# ADR-0005-rebuild-structure-file-tree-from-stored-files

> 작성일자
> 2026-06-09

## Status

Accepted

## Context

프로젝트 구조 생성 기능은 AI 서버의 SSE 이벤트를 통해 파일과 디렉토리 생성 결과를 수신한다. `file_created` 이벤트는 생성된 항목의 `path`, `type`, `depth`, `content`를 포함하며, ADR-0001에 따라 이 정보는 `structure_files`에 파일 단위로 저장된다.

상태 조회 API인 `GET /projects/structures/{projectId}`는 생성 상태와 함께 완료된 프로젝트의 `fileTree`를 반환해야 한다. 이때 `fileTree`의 기준 데이터를 어디에 둘지 결정해야 한다.

가능한 방식은 다음과 같다.

- `complete` 이벤트 payload에 포함된 최종 결과를 그대로 반환한다.
- `project.structure`에 저장된 최종 트리 JSON을 반환한다.
- `structure_files`에 저장된 파일/디렉토리 목록을 조회해 트리를 재구성한다.

`complete` 이벤트는 생성 완료 시점과 요약 정보를 알리는 이벤트이며, 파일별 저장 성공 여부를 보장하는 source of truth로 보기 어렵다. 또한 `project.structure`를 원본 저장소로 사용하면 ADR-0001의 파일 단위 저장 결정과 충돌한다.

## Decision

상태 조회 API의 `fileTree`는 `structure_files`에 저장된 파일/디렉토리 목록을 기준으로 재구성한다.

`complete` 이벤트 payload는 생성 완료 알림과 요약 정보로만 사용한다. 상태 조회의 `fileTree` 생성 기준으로 사용하지 않는다.

`project.structure`는 필요할 경우 성능 최적화를 위한 캐시로만 사용할 수 있다. 캐시를 사용하더라도 원본 데이터는 `structure_files`이며, 캐시 누락 또는 불일치 시 `structure_files`에서 다시 재구성할 수 있어야 한다.

트리 재구성 기준은 다음과 같다.

- `project_id`로 `structure_files`를 조회한다.
- `path`는 프로젝트 루트 기준 상대 경로로 해석한다.
- `/`를 기준으로 경로를 분리해 부모/자식 관계를 만든다.
- `type`이 `DIRECTORY`인 항목은 `children`을 가진다.
- `type`이 `FILE`인 항목은 기본적으로 빈 `children`을 가진다.
- 같은 경로가 중복 저장되는 경우 `project_id + path` unique 제약과 upsert 정책에 따라 최신 저장값 하나만 사용한다.
- 반환 순서는 클라이언트 렌더링이 안정적이도록 디렉토리를 파일보다 먼저 두고, 같은 타입 안에서는 이름 오름차순으로 정렬한다.

## Consequence

`GET /projects/structures/{projectId}`의 완료 결과는 실제 저장된 파일 목록과 일관된다. SSE 중계 중 `file_created` 저장에 실패한 항목은 상태 조회 결과에도 나타나지 않으므로, 저장 실패를 감지하고 생성 실패로 전환하는 처리가 중요해진다.

장점:

- 상태 조회, 파일 내용 조회, zip 다운로드가 모두 같은 원본 데이터인 `structure_files`를 기준으로 동작한다.
- `complete` 이벤트 payload 형식 변경이 상태 조회 응답 구조에 직접 영향을 주지 않는다.
- 캐시가 없어도 저장된 파일 목록만 있으면 결과 트리를 복구할 수 있다.
- 중복 이벤트가 발생해도 upsert된 최종 파일 목록으로 안정적인 트리를 만들 수 있다.

단점:

- 상태 조회 시 파일 목록을 트리로 변환하는 로직이 필요하다.
- 파일 수가 많아질 경우 매번 재구성하는 비용이 발생할 수 있다.
- 정렬, 누락된 부모 디렉토리 처리, 잘못된 path 검증 규칙을 구현에서 명확히 관리해야 한다.

구현 지침:

- 트리 생성 로직은 상태 조회 서비스 내부에 두거나 별도 assembler/helper로 분리한다.
- `path`는 저장 시점에 검증해 빈 경로, 절대 경로, `../` 포함 경로를 거부한다.
- 파일 경로의 중간 디렉토리가 별도 `DIRECTORY` 항목으로 저장되지 않은 경우에도 응답 트리에서는 필요한 부모 디렉토리 노드를 보강할 수 있다.
- `project.status`가 `COMPLETED`일 때만 `result.fileTree`를 반환하고, `PENDING`, `IN_PROGRESS`, `FAILED`에서는 `result`를 `null`로 반환한다.
- 성능 문제가 확인되면 `project.structure`에 재구성된 `fileTree` JSON을 캐시하되, 캐시 무효화 기준은 `structure_files` 변경 시점으로 둔다.
