package com.eventticketserver.common

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import com.redis.testcontainers.RedisContainer

interface IntegrationTestSupport {

    companion object {
        private const val POSTGRES_IMAGE = "postgres:15.10-alpine3.21"
        private const val REDIS_IMAGE = "redis:7.4.2-alpine3.21"
        private const val KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.3"
        private const val SCHEMA_REGISTRY_IMAGE = "confluentinc/cp-schema-registry:7.5.3"

        private val network = Network.newNetwork()

        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>(DockerImageName.parse(POSTGRES_IMAGE)).apply {
            start()
        }

        @JvmStatic
        val redis = RedisContainer(DockerImageName.parse(REDIS_IMAGE)).apply {
            start()
        }

        @JvmStatic
        val kafka = ConfluentKafkaContainer(DockerImageName.parse(KAFKA_IMAGE)).apply {
            withNetwork(network)
            withNetworkAliases("kafka") // 내부 통신용 별칭 설정
            start()
        }

        @JvmStatic
        val schemaRegistry = GenericContainer<Nothing>(DockerImageName.parse(SCHEMA_REGISTRY_IMAGE)).apply {
            withNetwork(network)
            withExposedPorts(8081)
            withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
            dependsOn(kafka)
            start()
        }

        @JvmStatic @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("spring.kafka.properties.schema.registry.url") { 
                "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(8081)}" 
            }
        }
    }
}
