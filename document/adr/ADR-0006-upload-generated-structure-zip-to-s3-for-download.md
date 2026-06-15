# ADR-0006-upload-generated-structure-zip-to-s3-for-download

> 작성일자
> 2026-06-14

## Status

Accepted

## Context

프로젝트 구조 생성 결과는 ADR-0001에 따라 `structure_files`에 파일 단위로 저장된다. 상태 조회와 파일 내용 조회도 이 저장 데이터를 기준으로 동작한다.

FEAT-2의 다운로드 API인 `POST /projects/structures/{projectId}/download`는 zip 다운로드 URL을 반환해야 하며, 명세에는 URL이 presigned 방식이고 만료 시간이 있다고 정의되어 있다. 따라서 API 서버가 명세에 없는 별도 파일 다운로드 엔드포인트를 공개하기보다, 다운로드 산출물인 zip 파일을 외부 스토리지에 저장하고 presigned URL을 반환하는 방식이 필요하다.

가능한 방식은 다음과 같다.

- API 서버가 요청마다 zip을 생성해 직접 응답한다.
- API 서버 내부에 명세 외 다운로드 엔드포인트를 만들고 해당 URL을 반환한다.
- `structure_files`에서 zip을 생성한 뒤 S3에 업로드하고 presigned URL을 반환한다.
- 생성된 개별 파일까지 모두 S3에 저장하고, S3 데이터를 기준으로 zip을 만든다.

API 서버가 직접 zip을 내려주거나 명세 외 엔드포인트를 추가하면 FEAT-2 명세의 presigned URL 정책과 맞지 않는다. 반대로 개별 파일까지 S3에 저장하면 현재 원본 저장소를 `structure_files`로 둔 결정과 책임이 중복된다.

## Decision

다운로드 API는 `structure_files`에 저장된 파일과 디렉토리 목록을 읽어 zip 파일을 생성한다. 생성된 zip 파일은 S3에 업로드하고, 클라이언트에는 S3 presigned GET URL을 반환한다.

S3는 생성 결과의 원본 저장소가 아니라 다운로드 산출물 저장소로 사용한다. 원본 데이터의 source of truth는 계속 `structure_files`이며, 파일 내용 조회와 상태 조회도 기존처럼 `structure_files`를 기준으로 처리한다.

다운로드 흐름은 다음과 같다.

- `POST /projects/structures/{projectId}/download` 요청을 받는다.
- `projectId`로 `structure_files`를 조회한다.
- 조회한 파일과 디렉토리 목록을 zip 파일로 압축한다.
- zip 파일을 S3에 업로드한다.
- 만료 시간이 있는 presigned GET URL, `expiresAt`, `fileName`을 반환한다.

S3 object key는 프로젝트별로 충돌하지 않도록 `{projectId}/structures/downloads/{fileName}` 형태를 기본으로 사용한다.

## Consequence

다운로드 API는 FEAT-2 명세에 있는 단일 엔드포인트만 공개하면서도 presigned URL 요구사항을 만족한다. 클라이언트는 API 서버를 통해 파일 바이트를 직접 받지 않고 S3 presigned URL로 zip을 다운로드한다.

장점:

- 명세에 없는 다운로드 파일 엔드포인트를 추가하지 않아 API surface가 늘어나지 않는다.
- API 서버가 대용량 zip 다운로드 연결을 오래 유지하지 않아도 된다.
- presigned URL 만료 시간으로 다운로드 접근 범위를 제한할 수 있다.
- 상태 조회, 파일 내용 조회, 다운로드 zip 생성이 모두 같은 원본 데이터인 `structure_files`를 기준으로 동작한다.
- S3는 zip 산출물에만 사용하므로 원본 파일 저장 책임이 중복되지 않는다.

단점:

- 다운로드 요청 시 zip 생성과 S3 업로드 비용이 발생한다.
- S3 설정, 권한, presigner 구성이 필요하다.
- S3 업로드 실패 시 다운로드 URL 발급도 실패하므로 재시도 또는 오류 처리가 필요하다.
- 같은 프로젝트에 대해 다운로드 요청이 반복되면 동일한 zip을 여러 번 생성하거나 업로드할 수 있다.

구현 지침:

- zip 생성 대상은 `structure_files`의 `project_id` 기준 파일/디렉토리 목록이다.
- 파일 엔트리는 저장된 `content`를 UTF-8 바이트로 기록한다.
- 디렉토리 엔트리는 `/`로 끝나는 zip entry로 기록한다.
- `fileName`은 `{projectName}_{YYYYMMDD}.zip` 규칙을 따르며, 파일 시스템에 안전하지 않은 문자는 치환한다.
- presigned URL 만료 시간은 응답의 `expiresAt`과 동일한 정책을 사용한다.
- S3 object의 `contentType`은 `application/zip`, `contentDisposition`은 attachment로 설정한다.
- 성능 문제가 확인되면 projectId와 구조 파일 변경 시점을 기준으로 유효한 zip 산출물을 재사용하는 캐시 정책을 추가할 수 있다.
