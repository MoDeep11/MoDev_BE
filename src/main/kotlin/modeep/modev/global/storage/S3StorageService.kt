package modeep.modev.global.storage

import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import modeep.modev.global.config.properties.S3Properties
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.Duration

@Component
class S3StorageService(
    private val s3Template: S3Template,
    private val s3Properties: S3Properties,
) {
    fun upload(
        objectKey: String,
        fileName: String,
        contentType: String,
        content: ByteArray,
    ) {
        val metadata =
            ObjectMetadata.builder()
                .contentType(contentType)
                .contentDisposition("attachment; filename=\"$fileName\"")
                .contentLength(content.size.toLong())
                .build()

        s3Template.upload(
            s3Properties.bucket,
            objectKey,
            ByteArrayInputStream(content),
            metadata,
        )
    }

    fun createPresignedGetUrl(
        objectKey: String,
        expiration: Duration,
    ): String =
        s3Template.createSignedGetURL(s3Properties.bucket, objectKey, expiration)
            .toString()
}
