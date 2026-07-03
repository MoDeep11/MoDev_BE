# ADR-0008-store-catalog-registry-versions-separately

> 작성일자
> 2026-06-20

## Status

Proposal

## Context

ADR-0007에서는 외부 레지스트리에서 버전 정보를 가져와 catalog 데이터에 반영하기로 했다.

하지만 `TechStack.version` 또는 `Dependency.version`을 최신 버전으로 덮어쓰면, 기존 프로젝트가 선택했던 버전과 현재 레지스트리 최신 버전을 구분하기 어렵다. 또한 catalog 조회 시 현재 설정된 버전이 최신인지 판단하려면 비교 대상이 되는 최신 버전 정보가 별도로 필요하다.

향후 사용 가능한 버전 목록 제공, 최신 여부 표시, 버전 변경 이력 확인도 필요할 수 있다.

## Decision

레지스트리에서 가져온 버전 목록은 catalog 엔티티에 직접 누적하지 않고, 별도의 버전 관리 테이블에 저장한다.

예상 테이블은 `catalog_registry_versions`로 둔다.

주요 컬럼:

- `target_type`: `TECH_STACK` 또는 `DEPENDENCY`
- `target_id`: 대상 catalog 엔티티 ID
- `version`
- `is_latest`
- `is_stable`
- `fetched_at`

`target_type`, `target_id`, `version` 조합을 복합 기본키로 사용한다. 별도 surrogate `id`는 두지 않는다.

`TechStack.version`과 `Dependency.version`은 현재 서비스에서 기본으로 사용할 버전으로 유지한다. 레지스트리 동기화는 최신 버전 정보를 `catalog_registry_versions`에 저장하고, catalog 조회 응답에서는 현재 version과 최신 version을 비교해 `isLatest`를 내려준다.

기존 프로젝트가 선택한 버전은 catalog 최신 버전 변경과 무관하게 유지되어야 하므로, `project_dependencies.version`에 선택 당시 버전을 저장하는 방향으로 구현한다.

## Consequence

catalog row를 버전마다 새로 만들지 않아 publicId와 기존 참조 관계를 안정적으로 유지할 수 있다.

장점:

- 최신 버전과 현재 기본 버전을 분리할 수 있다.
- catalog 응답에서 `isLatest`를 계산할 수 있다.
- 사용 가능한 버전 목록과 변경 이력을 확장하기 쉽다.
- 기존 프로젝트가 선택한 버전을 유지하는 정책과 충돌하지 않는다.

단점:

- 버전 관리 테이블과 동기화 저장 로직이 추가된다.
- 최신 버전 판단 시 catalog 테이블 외 추가 조회가 필요하다.

구현 지침:

- 같은 대상과 같은 버전은 중복 저장하지 않는다.
- `target_type`, `target_id`, `version` 복합키로 같은 대상과 같은 버전의 중복 저장을 막는다.
- 한 대상에는 하나의 `is_latest=true` 버전만 허용한다.
- prerelease, snapshot, beta, rc 버전은 기본적으로 `is_stable=false`로 저장한다.
- catalog 조회 응답에는 `latestVersion`, `isLatest`를 포함한다.
- 프로젝트 생성 시 선택된 dependency version은 `project_dependencies.version`에 별도로 저장한다.
