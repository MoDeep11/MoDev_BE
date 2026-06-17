package modeep.modev.global.storage

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import modeep.modev.global.config.properties.S3Properties
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.InputStream
import java.net.URI
import java.time.Duration
import kotlin.test.assertEquals

class S3StorageServiceTest {
    private val s3Template = mock(S3Template::class.java)
    private val service =
        S3StorageService(
            s3Template = s3Template,
            s3Properties = S3Properties(bucket = "test-bucket"),
        )

    @Test
    fun `uploads content with metadata`() {
        val inputCaptor = ArgumentCaptor.forClass(InputStream::class.java)
        val metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata::class.java)

        service.upload(
            objectKey = "structures/project/test.zip",
            fileName = "test.zip",
            contentType = "application/zip",
            content = "zip-content".toByteArray(),
        )

        verify(s3Template).upload(
            eq("test-bucket"),
            eq("structures/project/test.zip"),
            inputCaptor.capture(),
            metadataCaptor.capture(),
        )
        assertEquals("zip-content", inputCaptor.value.readBytes().toString(Charsets.UTF_8))
        assertEquals("application/zip", metadataCaptor.value.contentType)
        assertEquals("attachment; filename=\"test.zip\"", metadataCaptor.value.contentDisposition)
        assertEquals("zip-content".toByteArray().size.toLong(), metadataCaptor.value.contentLength)
    }

    @Test
    fun `creates presigned get url`() {
        val expiration = Duration.ofHours(1)
        `when`(
            s3Template.createSignedGetURL(
                "test-bucket",
                "structures/project/test.zip",
                expiration,
            ),
        ).thenReturn(URI("https://storage.example.com/test.zip?token=abc").toURL())

        val url = service.createPresignedGetUrl("structures/project/test.zip", expiration)

        assertEquals("https://storage.example.com/test.zip?token=abc", url)
    }
}
