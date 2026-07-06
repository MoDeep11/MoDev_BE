# ADR-0003-disable-nginx-buffering-and-send-heartbeat-for-sse

> 작성일자
> 2026-06-09

## Status

Accepted

## Context

프로젝트 구조 생성 기능은 SSE(Server-Sent Events)를 사용하여 생성 진행 상태를 클라이언트에 실시간으로 전달한다.

서버 앞단에 Nginx 같은 reverse proxy가 있으면 기본 설정에 따라 응답이 버퍼링될 수 있다. 이 경우 애플리케이션 서버가 SSE 이벤트를 전송하더라도 클라이언트는 이벤트를 즉시 받지 못하고, 프록시 버퍼가 flush될 때까지 대기할 수 있다.

또한 SSE 연결은 장시간 유지되는 HTTP 연결이다. 일정 시간 동안 데이터가 전송되지 않으면 브라우저, 프록시, 로드밸런서의 idle timeout에 의해 연결이 끊길 수 있다.

따라서 SSE의 실시간성과 연결 안정성을 보장하기 위해 프록시 버퍼링을 비활성화하고 주기적인 heartbeat 이벤트를 전송할 필요가 있다.

## Decision

프로젝트 구조 생성 SSE 엔드포인트에는 Nginx buffering 비활성화와 heartbeat 전송 정책을 적용한다.

Nginx 설정에서는 SSE 응답이 버퍼링되지 않도록 다음 설정을 적용한다.

```nginx
location /projects/structures/{projectId}/stream {
    proxy_pass http://backend;

    proxy_buffering off;
    proxy_cache off;

    proxy_http_version 1.1;
    proxy_set_header Connection "";
}
```

애플리케이션 응답에서도 필요한 경우 다음 헤더를 추가한다.

```text
X-Accel-Buffering: no
```

SSE 연결이 유휴 상태로 판단되어 끊기지 않도록 주기적으로 heartbeat 이벤트를 전송한다.

```text
event: heartbeat
data: ping
```

heartbeat 주기는 설정값으로 관리한다.

```yaml
stream:
  structure:
    timeout: 10m
    heartbeat-interval: 30s
```

적용 대상은 프로젝트 구조 생성 SSE 스트리밍 API이며, 주요 구현 대상은 `StreamStructureService`와 `GenerateStructureWorker`다.

## Consequence

장점:

- SSE 이벤트가 Nginx 버퍼링으로 지연되는 문제를 줄일 수 있다.
- 클라이언트가 생성 진행 상태를 실시간으로 수신할 수 있다.
- 장시간 작업 중에도 SSE 연결이 유지될 가능성이 높아진다.
- 프록시, 브라우저, 로드밸런서의 idle timeout으로 인한 연결 종료를 줄일 수 있다.
- timeout과 heartbeat 주기를 환경별로 조정할 수 있다.

단점:

- heartbeat 전송 로직이 추가된다.
- 연결 수가 많아질 경우 heartbeat 이벤트로 인한 네트워크 트래픽이 증가한다.
- Nginx 설정과 애플리케이션 설정을 함께 관리해야 한다.

고려했으나 채택하지 않은 대안:

- Nginx 기본 buffering 유지: 별도 설정은 필요 없지만 SSE 이벤트가 즉시 전달되지 않아 실시간성이 깨질 수 있다.
- heartbeat 미전송: 구현은 단순하지만 AI 생성 작업이 오래 걸릴 경우 연결이 중간에 끊길 수 있고, 클라이언트가 연결 종료를 작업 실패로 오해할 수 있다.

구현 지침:

- `StreamStructureService`는 작업 ID별 `SseEmitter`를 관리한다.
- 연결 생성 시 heartbeat 전송 작업을 시작한다.
- 작업 완료 또는 실패 시 완료/실패 이벤트를 전송하고 heartbeat를 중단한다.
- `SseEmitter.complete()` 호출 후 emitter 저장소에서 제거한다.
- timeout, error, completion 콜백에서도 emitter와 heartbeat 작업을 정리한다.
