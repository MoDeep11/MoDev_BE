package modeep.modev.domain.structure.service.vo

enum class StreamStructureEvent(
    val eventName: String,
) {
    CONNECTED("connected"),
    PROGRESS("progress"),
    FILE_CREATED("file_created"),
    COMPLETE("complete"),
    ERROR("error"),
    ;

    companion object {
        fun from(eventName: String): StreamStructureEvent? =
            entries.firstOrNull {
                it.eventName == eventName
            }
    }
}
