## 1. Maven Central (Java / Kotlin)

### 버전 목록 조회

```
GET https://search.maven.org/solrsearch/select
  ?q=g:{groupId}+AND+a:{artifactId}
  &core=gav
  &rows=20
  &wt=json
```

예시 (Spring Boot)

```
GET https://search.maven.org/solrsearch/select?q=g:org.springframework.boot+AND+a:spring-boot-starter&core=gav&rows=20&wt=json
```

응답

```json
{
  "response": {
    "docs": [
      { "g": "org.springframework.boot", "a": "spring-boot-starter", "v": "3.3.0", "p": "jar" },
      { "g": "org.springframework.boot", "a": "spring-boot-starter", "v": "3.2.5", "p": "jar" }
    ]
  }
}
```

추출 필드

- `v` → 버전

---

## 2. npm Registry (Node.js)

### 버전 목록 조회

```
GET https://registry.npmjs.org/{package-name}
```

예시 (Express)

```
GET https://registry.npmjs.org/express
```

응답

```json
{
  "name": "express",
  "versions": {
    "4.18.2": { ... },
    "4.17.3": { ... }
  },
  "dist-tags": {
    "latest": "4.18.2"
  }
}
```

추출 필드

- `versions` 키 목록 → 전체 버전 목록
- `dist-tags.latest` → 최신 안정 버전

---

## 3. PyPI (Python)

### 버전 목록 조회

```
GET https://pypi.org/pypi/{package-name}/json
```

예시 (FastAPI)

```
GET https://pypi.org/pypi/fastapi/json
```

응답

```json
{
  "info": {
    "name": "fastapi",
    "version": "0.111.0"
  },
  "releases": {
    "0.111.0": [ ... ],
    "0.110.3": [ ... ]
  }
}
```

추출 필드

- `releases` 키 목록 → 전체 버전 목록
- `info.version` → 최신 안정 버전

---

## 4. Go Proxy

### 버전 목록 조회

```
GET https://proxy.golang.org/{module}/@v/list
```

예시 (Gin)

```
GET https://proxy.golang.org/github.com/gin-gonic/gin/@v/list
```

응답

```
v1.9.1
v1.9.0
v1.8.2
```

추출 필드

- 줄바꿈으로 구분된 버전 문자열 목록

---

## 5. Docker Hub

### 태그(버전) 목록 조회

```
GET https://hub.docker.com/v2/repositories/{namespace}/{image}/tags?page_size=20
```

예시 (PostgreSQL)

```
GET https://hub.docker.com/v2/repositories/library/postgres/tags?page_size=20
```

응답

```json
{
  "results": [
    { "name": "16.3", "last_updated": "2024-05-01T00:00:00Z" },
    { "name": "15.7", "last_updated": "2024-04-01T00:00:00Z" }
  ]
}
```

추출 필드

- `results[].name` → 버전(태그)

---

## 6. Artifact Hub (Helm Chart)

### 버전 목록 조회

```
GET https://artifacthub.io/api/v1/packages/helm/{repo}/{chart}
```

예시 (Ingress Nginx)

```
GET https://artifacthub.io/api/v1/packages/helm/ingress-nginx/ingress-nginx
```

응답

```json
{
  "name": "ingress-nginx",
  "available_versions": [
    { "version": "4.10.1", "created_at": 1714000000 },
    { "version": "4.9.1",  "created_at": 1710000000 }
  ]
}
```

추출 필드

- `available_versions[].version` → 버전

---

## 7. GitHub Releases API

Kubernetes, Jenkins 등 GitHub에서 릴리즈를 관리하는 스택에 범용적으로 사용한다.

### 버전 목록 조회

```
GET https://api.github.com/repos/{owner}/{repo}/releases?per_page=20
```

예시 (Kubernetes)

```
GET https://api.github.com/repos/kubernetes/kubernetes/releases?per_page=20
```

예시 (Jenkins)

```
GET https://api.github.com/repos/jenkinsci/jenkins/releases?per_page=20
```

응답

```json
[
  { "tag_name": "v1.30.1", "published_at": "2024-05-14T00:00:00Z", "prerelease": false },
  { "tag_name": "v1.29.5", "published_at": "2024-04-17T00:00:00Z", "prerelease": false }
]
```

추출 필드

- `tag_name` → 버전
- `prerelease` → `false`인 것만 수집 (정식 릴리즈만)

인증

- 공개 레포는 인증 없이 호출 가능
- 단, 미인증 시 60회/시간 레이트 리밋 → `GITHUB_TOKEN` 사용 권장 (5,000회/시간)

---

## 8. 레지스트리별 요약

| 생태계 | 레지스트리 | Base URL | 인증 필요 |
| --- | --- | --- | --- |
| Java / Kotlin | Maven Central | `https://search.maven.org/solrsearch/select` | 없음 |
| Node.js | npm Registry | `https://registry.npmjs.org` | 없음 |
| Python | PyPI | `https://pypi.org/pypi` | 없음 |
| Go | Go Proxy | `https://proxy.golang.org` | 없음 |
| Docker 이미지 | Docker Hub | `https://hub.docker.com/v2/repositories` | 없음 |
| Helm Chart | Artifact Hub | `https://artifacthub.io/api/v1/packages/helm` | 없음 |
| GitHub 릴리즈 | GitHub Releases API | `https://api.github.com/repos` | 권장 (레이트 리밋) |

