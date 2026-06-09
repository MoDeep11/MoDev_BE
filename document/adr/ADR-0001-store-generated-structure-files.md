# ADR-0001-store-generated-structure-files

> 작성일자
> 2026-06-09

## Status

Accepted

## Context

프로젝트 구조 생성 기능은 AI 서버의 SSE 이벤트를 통해 생성 진행 상황을 수신한다. 이때 `file_created` 이벤트에는 생성된 파일 또는 디렉토리의 경로, 타입, 깊이, 파일 내용이 포함된다. `complete` 이벤트는 전체 생성이 끝났음을 알리는 역할을 한다.

기존 임시 구현은 최종 생성 결과를 `project.structure` 문자열 필드에 저장하는 방향이었다. 하지만 파일 내용 조회, zip 다운로드, 중복 이벤트 처리, 부분 실패 대응을 고려하면 전체 파일 내용을 하나의 문자열 컬럼에 저장하는 방식은 적합하지 않다.

특히 다음 요구사항을 만족해야 한다.

- `file_created` 이벤트를 받을 때마다 파일 단위로 저장한다.
- 특정 파일 내용을 경로 기준으로 조회할 수 있어야 한다.
- 생성 완료 후 전체 파일 목록을 기반으로 zip 다운로드를 만들 수 있어야 한다.
- SSE 재시도나 중복 이벤트가 발생해도 같은 파일이 중복 저장되지 않아야 한다.

## Decision

생성된 파일과 디렉토리는 `project.structure`에 누적 저장하지 않고, 별도 테이블인 `structure_files`에 파일 단위로 저장한다.

`structure_files`는 다음 정보를 가진다.

```text
structure_files
- id
- project_id
- type
- path
- depth
- content
- created_at
- updated_at
```

`project_id + path`에는 unique 제약을 둔다. 동일한 프로젝트에서 같은 경로의 `file_created` 이벤트가 다시 들어오면 insert가 아니라 update로 처리한다.

이벤트 처리 기준은 다음과 같다.

- `file_created`: 이벤트 data를 파싱해 `structure_files`에 upsert 저장하고, 클라이언트 SSE로 그대로 중계한다.
- `complete`: 프로젝트 상태를 `COMPLETED`로 변경하고, 필요하면 `project.structure`에 최종 파일 트리 JSON 또는 요약 캐시만 저장한다.
- `error` 또는 stream error: 프로젝트 상태를 `FAILED`로 변경하고 SSE 연결을 종료한다.

`project.structure`는 제거하지 않는다. 다만 파일 원본 저장소로 사용하지 않고, 상태 조회 API의 빠른 응답을 위한 최종 파일 트리 캐시 또는 생성 요약 저장 용도로만 사용한다.

## Consequence

`structure_files`가 생성 결과의 source of truth가 된다. 특정 파일 내용 조회와 zip 다운로드는 `project.structure`가 아니라 `structure_files`를 기준으로 구현한다.

장점:

- 파일 단위 조회가 단순해진다.
- zip 생성 시 전체 JSON 파싱 없이 파일 목록을 바로 사용할 수 있다.
- `project_id + path` unique 제약으로 중복 이벤트에 대해 멱등성을 확보할 수 있다.
- 파일 저장 실패를 생성 실패로 처리하기 쉬워져, 완료 상태인데 일부 파일이 누락되는 문제를 줄일 수 있다.

단점:

- `StructureFile` 엔티티와 repository가 추가된다.
- 생성 이벤트마다 DB write가 발생한다.
- 완료 상태와 파일 저장 상태의 정합성을 트랜잭션/에러 처리로 관리해야 한다.

구현 지침:

- `StructureFile` 엔티티를 추가하고 `project_id + path` unique 제약을 건다.
- `content`는 파일 내용 크기를 고려해 `@Lob` 또는 DB text 타입으로 매핑한다.
- 디렉토리의 `content`는 `null`로 저장한다.
- `path`는 프로젝트 루트 기준 상대 경로만 허용하고, 빈 경로, 절대 경로, `../` 포함 경로는 거부한다.
- `GenerateStructureWorker`는 `file_created` 수신 시 저장 서비스를 호출하고, `complete` 수신 시 프로젝트 완료 처리만 수행한다.
- `project.structure`는 최종 파일 트리 캐시가 필요한 경우에만 사용한다.
