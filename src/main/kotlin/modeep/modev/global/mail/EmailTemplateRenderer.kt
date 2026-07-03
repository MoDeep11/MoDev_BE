package modeep.modev.global.mail

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class EmailTemplateRenderer {
    fun render(
        templatePath: String,
        variables: Map<String, String> = emptyMap(),
    ): String {
        val template =
            ClassPathResource(templatePath)
                .inputStream
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

        return variables.entries.fold(template) { rendered, (key, value) ->
            rendered.replace("{{$key}}", value)
        }
    }
}

/* 사용 예시
val code = "123456"
val body =
  emailTemplateRenderer.render(
      templatePath = "templates/email/verify-email.html",
      variables =
          mapOf(
              "CODE_1" to code[0].toString(),
              "CODE_2" to code[1].toString(),
              "CODE_3" to code[2].toString(),
              "CODE_4" to code[3].toString(),
              "CODE_5" to code[4].toString(),
              "CODE_6" to code[5].toString(),
          ),
  )

mailService.send(
  MailMessage(
      to = email,
      subject = MailSubjects.VERIFY_EMAIL,
      body = body,
  ),
)
*/
