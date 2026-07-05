package modeep.modev.domain.structure.service

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.structure.controller.dto.response.ConnectedStreamResponse
import modeep.modev.domain.structure.controller.dto.response.StatusStreamResponse
import modeep.modev.domain.structure.service.vo.StreamStructureEvent
import modeep.modev.domain.structure.worker.StructureStatusService
import modeep.modev.global.config.properties.StructureStreamProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
class StreamStructureService(
    private val properties: StructureStreamProperties,
    @param:Qualifier("structureHeartbeatScheduler")
    private val heartbeatScheduler: ThreadPoolTaskScheduler,
    private val structureStatusService: StructureStatusService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val heartbeatTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun connect(projectId: UUID): SseEmitter {
        val id = projectId.toString()
        val emitter = SseEmitter(properties.timeoutMillis)
        complete(id)
        emitters[id] = emitter

        emitter.onCompletion {
            cleanup(id)
        }

        emitter.onTimeout {
            cleanup(id)
            emitter.complete()
        }

        emitter.onError {
            cleanup(id)
        }

        try {
            send(
                id = id,
                event = StreamStructureEvent.CONNECTED,
                data =
                    ConnectedStreamResponse(
                        projectId = id,
                        message = "연결되었습니다.",
                    ),
            )

            if (emitters[id] == emitter) {
                startHeartbeat(id)
            }

            // 이벤트 상태 검증
            val generation = structureStatusService.startGeneratingIfPending(projectId)
            send(
                id = id,
                event = StreamStructureEvent.STATUS,
                data =
                    StatusStreamResponse(
                        projectId = id,
                        status = generation.status,
                    ),
            )

            // 생성 종료 시(완료, 실패) emitter 정리
            if (generation.status == ProjectStatus.COMPLETED || generation.status == ProjectStatus.FAILED) {
                complete(id)
                return emitter
            }

            // PENDING -> GENERATING 상태 전이 시 이벤트 발행
            generation.event?.let(eventPublisher::publishEvent)
        } catch (e: Exception) {
            complete(id)
            throw e
        }

        return emitter
    }

    fun send(
        id: String,
        event: StreamStructureEvent,
        data: Any,
    ) {
        val emitter = emitters[id] ?: return

        try {
            synchronized(emitter) {
                emitter.send(
                    SseEmitter.event()
                        .name(event.eventName)
                        .data(data),
                )
            }
        } catch (e: Exception) {
            cleanup(id)
            emitter.completeWithError(e)
        }
    }

    fun complete(id: String) {
        cleanup(id)?.complete()
    }

    private fun startHeartbeat(id: String) {
        if (properties.heartbeatIntervalMillis <= 0) {
            return
        }

        val task =
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

        heartbeatTasks[id]?.cancel(true)
        heartbeatTasks[id] = task
    }

    private fun cleanup(id: String): SseEmitter? {
        heartbeatTasks.remove(id)?.cancel(true)
        return emitters.remove(id)
    }
}
