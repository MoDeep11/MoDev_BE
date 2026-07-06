package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.VerifyEmailResponse
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VerifyEmailService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    @Transactional
    fun execute(request: VerifyEmailResponse) {
        val email = request.email.trim().lowercase()

        val codeKey = "$CODE_KEY_PREFIX$email"
        val savedCode =
            redisTemplate.opsForValue().get(codeKey)
                ?: throw BusinessException(AuthErrorCode.VERIFY_CODE_EXPIRED)

        if (savedCode != request.code) {
            throw BusinessException(AuthErrorCode.VERIFY_CODE_INVALID)
        }

        redisTemplate.delete(codeKey)
    }

    private companion object {
        const val CODE_KEY_PREFIX = "auth:email-verification:code:"
    }
}
