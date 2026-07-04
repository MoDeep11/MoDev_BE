package modeep.modev.domain.structure.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.oshai.kotlinlogging.KotlinLogging
import modeep.modev.domain.structure.controller.dto.response.ErrorStreamResponse
import modeep.modev.domain.structure.controller.dto.response.FileCreatedStreamResponse
import modeep.modev.domain.structure.controller.dto.response.vo.StructureFileType
import modeep.modev.domain.structure.service.StreamStructureService
import modeep.modev.domain.structure.service.vo.StreamStructureEvent
import modeep.modev.domain.structure.worker.event.GenerateStructureEvent
import modeep.modev.global.exception.error.StructureErrorCode
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.reactive.function.client.WebClient

private val log = KotlinLogging.logger {}

@Component
class GenerateStructureWorker(
    private val streamStructureService: StreamStructureService,
    private val webClient: WebClient,
    private val structureStatusService: StructureStatusService,
    private val objectMapper: ObjectMapper,
) {
    @Async("structureExecutor")
    @TransactionalEventListener
    fun handle(event: GenerateStructureEvent) {
        val projectId = event.projectId
        val streamId = projectId.toString()

        try {
            webClient.post()
                .uri("/ai/structures/generate")
                .bodyValue(event)
                .retrieve()
                // 응답이 rawJson이기 때문에 String으로 받아서 처리
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
                        StreamStructureEvent.FILE_CREATED -> {
                            structureStatusService.saveFileCreated(projectId, parseFileCreated(data))
                        }

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
                    // 에러 처리
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
            // 실패 처리
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

    // 응답으로 받은 Json 파싱
    private fun parseFileCreated(data: String): FileCreatedStreamResponse {
        val node = objectMapper.readTree(data) as ObjectNode
        val type =
            StructureFileType.valueOf(
                node.path("type").asText().uppercase(),
            )

        return FileCreatedStreamResponse(
            type = type,
            path = node.path("path").asText(),
            depth = node.path("depth").asInt(),
            content = node.path("content").takeIf { !it.isMissingNode && !it.isNull }?.asText(),
        )
    }
}
