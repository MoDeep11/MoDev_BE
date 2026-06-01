package modeep.modev.global.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val name: String
    val status: HttpStatus
    val code: String
    val message: String
}
