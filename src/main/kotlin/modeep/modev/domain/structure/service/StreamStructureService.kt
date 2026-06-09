package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.service.vo.StreamStructureEvent
import modeep.modev.global.config.properties.StructureStreamProperties
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Service
class StreamStructureService(
    private val properties: StructureStreamProperties,
) {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()

    fun connect(id: String): SseEmitter {
        val emitter = SseEmitter(properties.timeoutMillis)
        emitters[id] = emitter

        emitter.onCompletion {
            emitters.remove(id)
        }

        emitter.onTimeout {
            emitters.remove(id)
        }

        emitter.onError {
            emitters.remove(id)
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
            emitter.send(
                SseEmitter.event()
                    .name(event.eventName)
                    .data(data),
            )
        } catch (e: Exception) {
            emitters.remove(id)
            emitter.completeWithError(e)
        }
    }

    fun complete(id: String) {
        emitters.remove(id)?.complete()
    }
}
