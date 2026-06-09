package modeep.modev.domain.structure.worker

import io.github.oshai.kotlinlogging.KotlinLogging
import modeep.modev.domain.structure.controller.dto.response.ErrorStreamResponse
import modeep.modev.domain.structure.service.StreamStructureService
import modeep.modev.domain.structure.service.vo.StreamStructureEvent
import modeep.modev.domain.structure.worker.event.GenerateStructureEvent
import modeep.modev.global.exception.error.StructureErrorCode
import org.springframework.context.event.EventListener
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val log = KotlinLogging.logger {}

@Component
class GenerateStructureWorker(
    private val streamStructureService: StreamStructureService,
    private val webClient: WebClient,
    private val structureStatusService: StructureStatusService,
) {
    @Async("structureExecutor")
    @EventListener
    fun handle(event: GenerateStructureEvent) {
        val projectId = event.projectId
        val streamId = projectId.toString()

        try {
            structureStatusService.markGenerating(projectId)

            webClient.post()
                .uri("/ai/structures/generate")
                .bodyValue(event)
                .retrieve()
                .bodyToFlux(object : ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .doOnNext { sse ->
                    val eventName =
                        sse.event()
                            ?: return@doOnNext

                    val streamEvent =
                        StreamStructureEvent.from(eventName)
                            ?: run {
                                log.warn { "Unknown event name: $eventName" }
                                return@doOnNext
                            }

                    val data =
                        sse.data()
                            ?: return@doOnNext

                    streamStructureService.send(
                        id = streamId,
                        event = streamEvent,
                        data = data,
                    )

                    when (streamEvent) {
                        StreamStructureEvent.COMPLETE -> {
                            structureStatusService.markCompleted(projectId, data)
                            streamStructureService.complete(streamId)
                        }

                        StreamStructureEvent.ERROR -> {
                            structureStatusService.markFailed(projectId)
                            streamStructureService.complete(streamId)
                        }

                        else -> Unit
                    }
                }
                .doOnError {
                    structureStatusService.markFailed(projectId)

                    streamStructureService.send(
                        id = streamId,
                        event = StreamStructureEvent.ERROR,
                        data =
                            ErrorStreamResponse(
                                code = StructureErrorCode.GENERATION_FAILED.name,
                                message = StructureErrorCode.GENERATION_FAILED.message,
                            ),
                    )

                    streamStructureService.complete(streamId)
                }
                .blockLast()
        } catch (e: Exception) {
            structureStatusService.markFailed(projectId)

            streamStructureService.send(
                id = streamId,
                event = StreamStructureEvent.ERROR,
                data =
                    ErrorStreamResponse(
                        code = StructureErrorCode.GENERATION_FAILED.name,
                        message = StructureErrorCode.GENERATION_FAILED.message,
                    ),
            )
        } finally {
            streamStructureService.complete(streamId)
        }
    }
}
