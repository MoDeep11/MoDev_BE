package modeep.modev.global.mail

data class MailMessage(
    val to: String,
    val subject: String,
    val body: String,
    val isHtml: Boolean = true,
    val cc: List<String> = emptyList(),
    val bcc: List<String> = emptyList(),
)

/*
to: 받는 이메일 주소
subject: 메일 제목
body: 메일 본문
isHtml: html로 본문 해석
cc: 참조 수신자
bcc: 숨은 참조 수신자
*/
