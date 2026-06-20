# ADR-0007-sync-catalog-data-from-package-registries

> 작성일자
> 2026-06-19

## Status

Proposal

## Context

현재 카탈로그 API는 `fields`, `tech_stacks`, `dependencies`에 저장된 데이터를 기준으로 기술 스택과 의존성 목록을 제공한다. `TechStack`과 `Dependency`에는 `version` 필드가 있으나, 외부 패키지 레지스트리의 최신 버전과 자동으로 동기화하는 구조는 아직 없다.

`document/registry-spec.md`에는 Maven Central, npm Registry, PyPI, Go Proxy, Docker Hub, Artifact Hub, GitHub Releases API에서 버전 목록을 조회하는 방법이 정의되어 있다. 각 레지스트리는 응답 형식과 최신 버전 판단 기준이 다르므로, 카탈로그 데이터 갱신 로직이 특정 레지스트리 응답 구조에 직접 결합되면 유지보수가 어려워진다.

필요한 기능은 다음과 같다.

- 정기적으로 외부 레지스트리 API를 호출해 카탈로그 데이터의 버전 정보를 갱신한다.
- 운영자가 필요할 때 특정 카탈로그 항목의 레지스트리 정보를 API로 조회하거나 갱신할 수 있어야 한다.
- 외부 레지스트리에서 가져온 결과는 별도 임시 응답으로만 사용하지 않고, 서비스의 source of truth인 catalog 데이터로 저장해야 한다.
- GitHub Releases API는 미인증 호출 시 rate limit이 낮으므로 `GITHUB_TOKEN` 사용을 고려해야 한다.
- 외부 API 장애, rate limit, 응답 형식 차이로 인해 전체 카탈로그 갱신이 실패하지 않도록 항목 단위 실패 처리가 필요하다.

## Decision

외부 레지스트리 동기화 기능은 catalog 도메인의 데이터 갱신 책임으로 구현한다. 정기 갱신은 Spring Scheduler를 사용하고, 외부 레지스트리 조회는 레지스트리별 client와 공통 sync service로 분리한다.

레지스트리에서 가져온 버전 정보는 `TechStack.version` 또는 `Dependency.version`에 저장한다. catalog API는 기존처럼 DB에 저장된 catalog 데이터를 반환하며, 외부 레지스트리 API를 조회 API 응답 시점마다 직접 호출하지 않는다.

레지스트리 동기화 방식은 다음과 같다.

- scheduler가 정해진 주기에 동기화 대상 catalog 항목을 조회한다.
- 각 항목의 ecosystem, registry type, package identifier를 기준으로 적절한 registry client를 선택한다.
- registry client는 외부 API 응답을 공통 모델인 `RegistryVersionResult`로 변환한다.
- sync service는 최신 안정 버전을 선택하고 catalog 엔티티의 `version` 값을 갱신한다.
- 항목 단위로 성공/실패를 기록하고, 한 항목 실패가 전체 동기화를 중단하지 않도록 처리한다.

수동 API도 제공한다.

- 특정 catalog 항목에 대해 외부 레지스트리에서 버전 정보를 조회하는 API를 제공한다.
- 필요 시 조회 결과를 catalog 데이터에 반영하는 갱신 API를 제공한다.
- 이 API는 운영성 기능이므로 인증/권한 정책을 적용하고, 공개 catalog 조회 API와 분리한다.

레지스트리별 매핑은 `document/registry-spec.md`의 정의를 따른다.

| 대상 | 레지스트리 | 추출 기준 |
| --- | --- | --- |
| Java / Kotlin | Maven Central | `response.docs[].v` |
| Node.js | npm Registry | `dist-tags.latest`, `versions` 키 목록 |
| Python | PyPI | `info.version`, `releases` 키 목록 |
| Go | Go Proxy | 줄 단위 버전 목록 |
| Docker 이미지 | Docker Hub | `results[].name` |
| Helm Chart | Artifact Hub | `available_versions[].version` |
| GitHub 릴리즈 | GitHub Releases API | `prerelease=false`인 `tag_name` |

## Consequence

카탈로그 API는 외부 레지스트리 장애와 무관하게 DB에 저장된 데이터를 안정적으로 반환할 수 있다. 외부 최신 버전 반영은 scheduler 또는 운영 API를 통해 비동기로 수행된다.

장점:

- catalog 데이터가 서비스 내부의 source of truth로 유지된다.
- 사용자 요청마다 외부 레지스트리를 호출하지 않아 응답 속도와 안정성이 좋아진다.
- 레지스트리별 응답 형식 차이를 client 계층에 격리할 수 있다.
- scheduler 기반으로 최신 버전 정보를 주기적으로 반영할 수 있다.
- 운영 API를 통해 특정 항목만 즉시 조회하거나 갱신할 수 있다.

단점:

- scheduler, 외부 API client, 실패 기록 등 운영성 구현이 추가된다.
- 저장된 버전 정보는 마지막 동기화 시점 기준이므로 실시간 최신 상태와 차이가 날 수 있다.
- GitHub Releases API 등 일부 외부 API는 rate limit과 인증 토큰 관리가 필요하다.
- 현재 catalog 엔티티의 `version` 필드는 `val`이므로 실제 갱신을 위해 변경 메서드 또는 mutable 필드 설계가 필요하다.

구현 계획:

1. catalog 항목에 레지스트리 조회를 위한 메타데이터를 추가한다.
   - registry type: `MAVEN_CENTRAL`, `NPM`, `PYPI`, `GO_PROXY`, `DOCKER_HUB`, `ARTIFACT_HUB`, `GITHUB_RELEASES`
   - package identifier: Maven은 `groupId`/`artifactId`, npm/PyPI는 package name, GitHub는 `owner`/`repo` 등 레지스트리별 식별자
   - 자동 동기화 여부, 마지막 동기화 시각, 마지막 동기화 실패 사유

2. 레지스트리 client 계층을 추가한다.
   - 공통 인터페이스: `RegistryClient`
   - 공통 반환 모델: `RegistryVersionResult(latestVersion, versions, fetchedAt)`
   - 레지스트리별 구현체: Maven, npm, PyPI, Go Proxy, Docker Hub, Artifact Hub, GitHub Releases
   - GitHub client는 `GITHUB_TOKEN` 설정이 있으면 Authorization header를 사용한다.

3. catalog version sync service를 추가한다.
   - 동기화 대상 catalog 항목을 조회한다.
   - registry type에 맞는 client를 선택한다.
   - 최신 안정 버전을 계산해 `TechStack.version` 또는 `Dependency.version`에 저장한다.
   - 항목 단위 트랜잭션 또는 실패 격리를 적용해 일부 실패가 전체 작업을 중단하지 않게 한다.

4. scheduler를 추가한다.
   - `@EnableScheduling`을 활성화한다.
   - `@Scheduled` 또는 `TaskScheduler` 기반으로 정기 동기화를 실행한다.
   - 주기는 설정값으로 분리한다. 예: `catalog.registry.sync.cron`
   - 동시에 여러 인스턴스에서 실행될 수 있는 배포 환경이면 lock 정책을 추가로 검토한다.

5. 운영 API를 추가한다.
   - 특정 catalog 항목의 레지스트리 버전 목록 조회 API
   - 특정 catalog 항목의 버전 정보를 즉시 갱신하는 API
   - 전체 catalog 동기화 트리거 API는 rate limit과 부하를 고려해 관리자 권한으로 제한한다.

6. 관측성과 실패 처리를 추가한다.
   - 동기화 성공/실패 로그를 남긴다.
   - 외부 API timeout, 4xx/5xx, 응답 파싱 실패를 구분한다.
   - 마지막 성공 시각과 실패 사유를 저장하거나 운영 로그로 확인 가능하게 한다.
   - 실패한 항목은 다음 scheduler 주기에 재시도한다.

7. 테스트를 추가한다.
   - 레지스트리별 응답 파싱 테스트
   - sync service의 version 갱신 테스트
   - 외부 API 실패 시 항목 단위 실패 격리 테스트
   - 수동 API 요청/응답 테스트

구현 시 주의사항:

- catalog 조회 API는 외부 레지스트리를 직접 호출하지 않는다.
- 외부 API timeout은 짧게 설정하고, scheduler 전체 작업 시간이 과도하게 길어지지 않도록 한다.
- prerelease, snapshot, beta, rc 버전은 기본적으로 최신 안정 버전에서 제외한다.
- 기존 프로젝트가 참조 중인 catalog publicId는 변경하지 않는다.
- 외부 레지스트리에서 항목을 찾지 못해도 기존 catalog 데이터를 삭제하지 않는다.
