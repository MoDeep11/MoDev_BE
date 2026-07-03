# ADR-0004-use-thread-pool-task-scheduler-for-sse-heartbeat

> 작성일자
> 2026-06-09

## Status

Accepted

## Context

ADR-0003에서 프로젝트 구조 생성 SSE 연결의 안정성을 위해 heartbeat 이벤트를 주기적으로 전송하기로 결정했다.

heartbeat는 일반 구조 생성 이벤트와 성격이 다르다. `progress`, `file_created`, `complete` 이벤트는 AI 서버의 응답 흐름에 따라 전송되지만, heartbeat는 SSE 연결이 유지되는 동안 일정 간격으로 반복 전송되어야 한다.

기존 비동기 설정인 `structureExecutor`는 `@Async` worker를 실행하기 위한 `ThreadPoolTaskExecutor`다. 이 executor는 작업을 비동기로 실행하는 데 적합하지만, `scheduleAtFixedRate` 같은 주기 실행 책임을 표현하기에는 적합하지 않다.

따라서 SSE heartbeat를 어떤 방식으로 예약 실행할지 결정이 필요하다.

## Decision

SSE heartbeat 전송에는 `ThreadPoolTaskScheduler`를 사용한다.

`AsyncConfig`에 heartbeat 전용 scheduler bean을 추가한다.

```kotlin
@Bean("structureHeartbeatScheduler")
fun structureHeartbeatScheduler(): ThreadPoolTaskScheduler {
    return ThreadPoolTaskScheduler().apply {
        setPoolSize(1)
        setThreadNamePrefix("structure-heartbeat-")
        setDaemon(true)
        initialize()
    }
}
```

`StreamStructureService`는 이 scheduler를 주입받아 연결별 heartbeat task를 등록한다.

```kotlin
heartbeatScheduler.scheduleAtFixedRate(
    {
        send(
            id = id,
            event = StreamStructureEvent.HEARTBEAT,
            data = "ping",
        )
    },
    Duration.ofMillis(properties.heartbeatIntervalMillis),
)
```

연결이 완료, 실패, timeout, error 상태가 되면 해당 연결의 heartbeat task를 취소하고 emitter를 제거한다.

## Consequence

장점:

- 비동기 worker 실행 책임과 heartbeat 예약 실행 책임이 분리된다.
- Spring이 관리하는 scheduler bean을 사용하므로 직접 `Executors.newSingleThreadScheduledExecutor`를 생성하고 종료하는 코드가 필요 없다.
- thread name, pool size, daemon 여부를 설정으로 명확히 관리할 수 있다.
- 추후 heartbeat 연결 수가 증가하면 scheduler pool size를 조정해 대응할 수 있다.
- 테스트와 운영 설정에서 scheduler bean을 교체하거나 조정하기 쉽다.

단점:

- heartbeat 전용 bean이 추가된다.
- 연결마다 scheduled task가 생기므로 연결 수가 많아질 경우 task 수와 전송 비용을 모니터링해야 한다.
- scheduler pool size가 너무 작으면 heartbeat 전송이 지연될 수 있다.

고려했으나 채택하지 않은 대안:

- `Executors.newSingleThreadScheduledExecutor`를 서비스 내부에서 직접 생성: 구현은 단순하지만 lifecycle 관리가 서비스 코드에 섞이고, Spring bean 설정과 모니터링 대상에서 벗어난다.
- `ThreadPoolTaskExecutor` 재사용: `@Async` 작업 실행에는 적합하지만 주기 실행 API를 제공하지 않는다.
- `@Scheduled`로 전체 emitter 순회: 구현은 가능하지만 연결별 task 정리와 실패 처리가 덜 명확하고, 모든 연결을 매 주기마다 순회해야 한다.

구현 지침:

- `ThreadPoolTaskScheduler`는 bean으로 등록하고 `StreamStructureService`에 주입한다.
- heartbeat interval은 `stream.structure.heartbeat-interval-millis` 설정값으로 관리한다.
- interval이 0 이하이면 heartbeat를 시작하지 않는다.
- 연결 종료 경로에서는 반드시 heartbeat task를 cancel 한다.
- emitter 전송은 heartbeat와 일반 이벤트가 동시에 호출될 수 있으므로 동기화해서 처리한다.
