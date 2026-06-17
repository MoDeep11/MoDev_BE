package modeep.modev.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableRetry
@EnableScheduling
@ConfigurationPropertiesScan
class AppConfig
