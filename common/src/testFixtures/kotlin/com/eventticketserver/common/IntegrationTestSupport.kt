package com.eventticketserver.common

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import com.redis.testcontainers.RedisContainer

@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestSupport {

    companion object {
        private const val POSTGRES_IMAGE = "postgres:15.10-alpine3.21"
        private const val REDIS_IMAGE = "redis:7.4.2-alpine3.21"
        private const val KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.3"
        private const val SCHEMA_REGISTRY_IMAGE = "confluentinc/cp-schema-registry:7.5.3"
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
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
    }

    @Bean
    fun schemaRegistryContainer(kafka: ConfluentKafkaContainer): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse(SCHEMA_REGISTRY_IMAGE))
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafka.networkAliases[0] + ":9092")
            .withNetwork(kafka.network)
            .dependsOn(kafka)
    }
}