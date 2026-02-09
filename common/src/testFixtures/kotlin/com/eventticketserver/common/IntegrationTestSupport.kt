package com.eventticketserver.common

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import com.redis.testcontainers.RedisContainer

@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestSupport {

    companion object {
        private const val POSTGRES_IMAGE = "postgres:15-alpine"
        private const val REDIS_IMAGE = "redis:7-alpine"
        private const val KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.0"
    }

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
    }

    @Bean
    @ServiceConnection
    fun redisContainer(): RedisContainer {
        return RedisContainer(DockerImageName.parse(REDIS_IMAGE))
    }

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer {
        return KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
    }
}