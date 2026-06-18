package modeep.modev.global.mail

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailTemplateRendererTest {
    private val renderer = EmailTemplateRenderer()

    @Test
    fun `renders verify email template with code placeholders`() {
        val rendered =
            renderer.render(
                templatePath = "templates/email/verify-email.html",
                variables =
                    mapOf(
                        "CODE_1" to "1",
                        "CODE_2" to "2",
                        "CODE_3" to "3",
                        "CODE_4" to "4",
                        "CODE_5" to "5",
                        "CODE_6" to "6",
                    ),
            )

        assertTrue(rendered.contains("MoDev 이메일 인증"))
        assertTrue(rendered.contains("1"))
        assertFalse(rendered.contains("{{CODE_1}}"))
    }

    @Test
    fun `renders reset password template with url placeholder`() {
        val resetPasswordUrl = "https://modev.dev/reset-password?token=test-token"

        val rendered =
            renderer.render(
                templatePath = "templates/email/reset-password.html",
                variables = mapOf("RESET_PASSWORD_URL" to resetPasswordUrl),
            )

        assertTrue(rendered.contains(resetPasswordUrl))
        assertFalse(rendered.contains("{{RESET_PASSWORD_URL}}"))
    }
}
