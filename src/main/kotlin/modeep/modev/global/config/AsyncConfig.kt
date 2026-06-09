package modeep.modev.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean("structureExecutor")
    fun structureExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 8
            queueCapacity = 100
            setThreadNamePrefix("structure-")
            initialize()
        }
    }

    @Bean("structureHeartbeatScheduler")
    fun structureHeartbeatScheduler(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            setPoolSize(1)
            setThreadNamePrefix("structure-heartbeat-")
            setDaemon(true)
            initialize()
        }
    }
}
