# Contributing Guide

프로젝트에 기여해 주셔서 감사합니다.

이 문서는 프로젝트에 기여하는 방법, 개발 환경 설정, 코딩 컨벤션 및 브랜치 전략을 설명합니다.

---

# 💡 기여 방법 (How to Contribute)

## 1. Issue 생성

새로운 기능 제안, 버그 제보, 문서 개선 요청 등은 GitHub Issue를 통해 등록합니다.

### Bug Report

버그를 발견한 경우 아래 내용을 포함해 주세요.

* 발생 환경 (OS, JDK 버전 등)
* 재현 방법
* 기대 결과
* 실제 결과
* 관련 로그 또는 스크린샷

### Feature Request

새로운 기능을 제안하는 경우 아래 내용을 포함해 주세요.

* 기능 설명
* 기대 효과
* 구현 아이디어 (선택)

---

## 2. Pull Request 생성

작업이 완료되면 Pull Request(PR)를 생성합니다.

### PR 작성 규칙

* 하나의 PR은 하나의 목적만 포함합니다.
* PR 제목은 커밋 컨벤션을 따릅니다.
* 변경 내용을 명확하게 작성합니다.
* 관련 Issue가 있다면 연결합니다.

### 리뷰 프로세스

1. PR 생성
2. 코드 리뷰 진행
3. 승인(Approve) 후 Merge

---

# 🛠️ 개발 환경 설정 (Development Setup)

## 요구 사항

* JDK 21
* Gradle 8.x 이상
* Git

## 프로젝트 클론

```bash
git clone https://github.com/MoDeep11/MoDev_BE.git modev

cd modev

npm install
```

## 애플리케이션 실행

### bash

```bash
./gradlew bootRun
```

## 테스트 실행

```bash
./gradlew test
```

---

# 📝 코딩 스타일 및 컨벤션

## 코드 포맷팅 (Style Guides)

본 프로젝트는 **Ktlint**를 사용하여 Kotlin 코드 스타일을 관리합니다.

### 자동 포매팅

프로젝트에는 Husky 기반 Git Hook이 적용되어 있습니다.

커밋 시 자동으로 다음 작업이 수행됩니다.

1. `ktlintFormat` 실행
2. 포멧팅된 파일 자동 Stage
3. 커밋 진행

따라서 별도로 포맷팅 명령어를 실행할 필요가 없습니다.

### 수동 실행

필요한 경우 아래 명령어를 통해 직접 실행할 수 있습니다.

```bash
./gradlew ktlintFormat
```

### 포맷 검사

```bash
./gradlew ktlintCheck
```

모든 기여자는 Ktlint 규칙을 준수해야 하며, 모든 PR은 ktlint 검사를 통과해야 합니다.

---

## 커밋 메시지 컨벤션

Conventional Commits를 사용합니다.

형식

```text
type(scope): subject
```

예시

```text
feat(auth): 로그인 API 개발
fix(user): 회원 조회 오류 수정
docs(readme): 설치 가이드 수정
test(auth): 로그인 테스트 추가
chore(deps): 의존성 업데이트
```

### Type 목록

| Type     | 설명             |
| -------- | -------------- |
| feat     | 기능 추가          |
| fix      | 버그 수정          |
| refactor | 리팩토링           |
| docs     | 문서 수정          |
| test     | 테스트 추가 및 수정    |
| chore    | 빌드, 설정, 의존성 변경 |

---

## 브랜치 전략

Git Flow 전략을 사용합니다.

### 주요 브랜치

| 브랜치     | 설명        |
| ------- | --------- |
| main    | 운영 배포 브랜치 |
| develop | 개발 통합 브랜치 |

### 작업 브랜치

브랜치 명은 `{type}/{issue-id}-name` 으로 작성합니다.

기능 개발

```text
feature/기능명
```

예시

```text
feature/8-login-api
feature/23-user-profile
```

버그 수정

```text
fix/이슈명
```

예시

```text
fix/12-login-error
```

### 작업 흐름

```text
develop
 └─ feature/login-api
 └─ feature/user-profile
```

1. develop 브랜치에서 feature 브랜치 생성
2. 기능 개발
3. PR 생성
4. 코드 리뷰
5. develop 브랜치로 Merge

