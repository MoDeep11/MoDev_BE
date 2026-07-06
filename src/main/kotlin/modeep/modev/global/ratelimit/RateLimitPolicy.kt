package modeep.modev.global.ratelimit

enum class RateLimitPolicy(
    val capacity: Long,
    val refillTokens: Long,
    val refillSeconds: Long,
) {
    // 로그인, 회원가입, 인증 코드 확인
    AUTH(10, 10, 60),

    // 토큰 재발급
    TOKEN_REFRESH(20, 20, 60),

    // 이메일 발송 (분당)
    EMAIL_SEND_PER_MINUTE(1, 1, 60),

    // 이메일 발송 (일일)
    EMAIL_SEND_PER_DAY(20, 20, 60 * 60 * 24),

    // 일반 API
    GENERAL_API(200, 200, 60),

    // AI 생성
    AI_GENERATION(5, 5, 60),

    // ZIP 다운로드
    ZIP_DOWNLOAD(20, 20, 60),
}
