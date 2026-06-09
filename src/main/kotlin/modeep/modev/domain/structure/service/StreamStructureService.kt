package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.controller.dto.response.ConnectedStreamResponse
import modeep.modev.domain.structure.service.vo.StreamStructureEvent
import modeep.modev.global.config.properties.StructureStreamProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
class StreamStructureService(
    private val properties: StructureStreamProperties,
    @param:Qualifier("structureHeartbeatScheduler")
    private val heartbeatScheduler: ThreadPoolTaskScheduler,
) {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val heartbeatTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun connect(id: String): SseEmitter {
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

        send(
            id = id,
            event = StreamStructureEvent.CONNECTED,
            data =
                ConnectedStreamResponse(
                    projectId = id,
                    message = "연결되었습니다. 생성을 시작합니다.",
                ),
        )

        startHeartbeat(id)

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
